package org.hibernate.ogm.hiking.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.hibernate.ogm.hiking.model.Hike;
import org.hibernate.ogm.hiking.model.Person;
import org.hibernate.ogm.hiking.model.Section;
import org.hibernate.ogm.hiking.model.Trip;
import org.hibernate.ogm.hiking.repository.HikeRepository;
import org.hibernate.ogm.hiking.repository.TripRepository;
import org.hibernate.ogm.hiking.rest.model.ExternalHike;

@Path("/hikes")
@Stateless
public class HikeResource {

	@Inject
	private HikeRepository hikeRepository;

	@Inject
	private TripRepository tripRepository;

	public HikeResource() {
	}

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public ExternalHike getHikeById(@PathParam("id") long hikeId) {
		return new ExternalHike( hikeRepository.getHikeById( hikeId ) );
	}

	@GET
	@Path("/")
	@Produces("application/json")
	public List<ExternalHike> findHikes(@QueryParam("q") String searchTerm) {
		List<Hike> hikes = searchTerm != null ? hikeRepository.getHikesByFromOrTo(searchTerm) : hikeRepository.getAllHikes();
		List<ExternalHike> descriptions = new ArrayList<>( hikes.size() );

		for ( Hike hike : hikes ) {
			descriptions.add( new ExternalHike( hike ) );
		}

		return descriptions;
	}

	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json")
	public ExternalHike createHike(ExternalHike externalHike) {
		Hike hike = new Hike( externalHike.getFrom(), externalHike.getTo() );
		Trip trip = null;

		if ( externalHike.getRecommendedTrip() != null ) {
			trip = tripRepository.getTripById( externalHike.getRecommendedTrip().getId() );
		}

		for (Section section : externalHike.getSections() ) {
			hike.sections.add( section );
		}

		hikeRepository.createHike( hike, trip );

		return externalHike;
	}

	@PUT
	@Path("/{id}")
	@Consumes("application/json")
	@Produces("application/json")
	public ExternalHike updateHike(ExternalHike externalHike) {

		Hike hike = hikeRepository.getHikeById( externalHike.getId() );

		hike.start = externalHike.getFrom();
		hike.destination = externalHike.getTo();

		if ( externalHike.getRecommendedTrip() != null ) {
			Trip recommendedTrip = tripRepository.getTripById( externalHike.getRecommendedTrip().getId() );
			hike.recommendedTrip = recommendedTrip;
			recommendedTrip.availableHikes.add( hike );
		}

		hike.sections.clear();
		hike.sections.addAll( externalHike.getSections() );

		return externalHike;
	}

	@DELETE
	@Path("/{id}")
	public void deleteHike(@PathParam("id") long id) {
		hikeRepository.deleteHike( id );
	}
}
