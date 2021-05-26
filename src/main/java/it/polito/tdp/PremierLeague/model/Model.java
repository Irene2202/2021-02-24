package it.polito.tdp.PremierLeague.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer, Player> idMap;
	
	public Model() {
		this.dao=new PremierLeagueDAO();
		idMap=new HashMap<>();
		dao.listAllPlayers(idMap);
	}
	
	public void creaGrafo(Match m) {
		grafo=new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		//vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(m, idMap));
		
		//archi
		for(Adiacenza a: dao.getArchi(m, idMap)) {
			if(a.getPeso()>=0) {
				//p1 meglio di p2
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(grafo, a.getP1(), a.getP2(), a.getPeso());
				}
			} else {
				//p2 meglio di p1
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(grafo, a.getP2(), a.getP1(), (-1)*a.getPeso());
				}
			}
		}
		
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public GiocatoreMigliore getMigliore() {
		if(grafo==null)
			return null;
		
		Player best=null;
		Double maxDelta=(double)Integer.MIN_VALUE;
		
		for(Player p: this.grafo.vertexSet()) {
			//calcolo somma pesi archi uscenti
			double pesoUscente=0;
			for(DefaultWeightedEdge d:this.grafo.outgoingEdgesOf(p)) {
				pesoUscente+=grafo.getEdgeWeight(d);
			}
			
			//calcolo somma pesi archi entranti
			double pesoEntrante=0;
			for(DefaultWeightedEdge d: this.grafo.incomingEdgesOf(p)) {
				pesoEntrante+=grafo.getEdgeWeight(d);
			}
			
			double delta=pesoUscente-pesoEntrante;
			if(delta>maxDelta) {
				best=p;
				maxDelta=delta;
			}
		}
		
		return new GiocatoreMigliore(best, maxDelta);
	}
	
	public List<Match> getAllMatches(){
		return dao.listAllMatches();
	}

	public Graph<Player, DefaultWeightedEdge> getGrafo() {
		return this.grafo;
	}
	
}
