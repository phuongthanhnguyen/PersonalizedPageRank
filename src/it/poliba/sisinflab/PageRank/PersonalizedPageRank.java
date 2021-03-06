package it.poliba.sisinflab.PageRank;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PersonalizedPageRank {
	
	SparseMatrix transMatrix;
	private int N;
	private float damping;
	private int iteration;
	SparseVector danglings;
	SparseVector w;
	
	private String filename;
	
	
	public PersonalizedPageRank(String fname){		
		this.damping=0.85f;
		this.iteration=8;
		this.filename=fname;
		this.N=0;
		readGraph2();
	}
	
	public PersonalizedPageRank(String fname, int size){		
		this.damping=0.85f;
		this.iteration=5;
		this.filename=fname;
		this.N=size;
		readGraph2();	
	}
	
	
	public PersonalizedPageRank(Graph graph){		
		this.damping=0.85f;	
		this.iteration=5;
		this.filename="";
		this.N=graph.numNodes();
		readGraph3(graph);	
	}

	
	public SparseVector calculatePersonalizedPageRankVector(int biasIndex){		
		
		SparseVector PPRV = new SparseVector(N);		
		SparseVector tmp1 = new SparseVector(N);
		SparseVector tmp2 = new SparseVector(N);
		SparseVector tmp3 = new SparseVector(N);
		SparseVector ret = new SparseVector(N);		
		SparseVector personalizedVector = new SparseVector(N);
				
		double val = (double)1/N;
		
		for(int i=0;i<N;i++){
			PPRV.put(i, val);			
		}
		
		/*give bias towards a specified node in the graph*/
		personalizedVector.put(biasIndex, (double)1.0);
		
		int r=0;		
		tmp3=personalizedVector.scale(1-this.damping);
						
		while (r<this.iteration) {			
			tmp1 = new SparseVector(N);
			tmp2 = new SparseVector(N);					
			tmp1=transMatrix.times(PPRV);		
			tmp1.scale(this.damping);			
			val=this.damping*PPRV.dot(this.danglings);		
			tmp2=w.scale(val);
			ret = new SparseVector(N);
			ret = tmp1.plus(tmp2);
			ret = ret.plus(tmp3);			
			PPRV = ret;			
			r+=1;
		}		
									
		return PPRV;		
	}
	
	
	public SparseVector calculatePersonalizedPageRankVector(){		
		
		SparseVector PPRV = new SparseVector(N);		
		SparseVector tmp1 = new SparseVector(N);
		SparseVector tmp2 = new SparseVector(N);
		SparseVector tmp3 = new SparseVector(N);
		SparseVector ret = new SparseVector(N);		
		SparseVector personalizedVector = new SparseVector(N);
				
		double val = (double)1/N;
		
		for(int i=0;i<N;i++){
			PPRV.put(i, val);			
		}
		
		for(int i=0;i<N;i++){
			personalizedVector.put(i, (double)1.0);			
		}
			
		
		int r=0;		
		tmp3=personalizedVector.scale(1-this.damping);
						
		while (r<this.iteration) {			
			tmp1 = new SparseVector(N);
			tmp2 = new SparseVector(N);					
			tmp1=transMatrix.times(PPRV);		
			tmp1.scale(this.damping);			
			val=this.damping*PPRV.dot(this.danglings);		
			tmp2=w.scale(val);
			ret = new SparseVector(N);
			ret = tmp1.plus(tmp2);
			ret = ret.plus(tmp3);			
			PPRV = ret;			
			r+=1;
		}		
									
		return PPRV;		
	}

		
	public void printVector(SparseVector vector){		
		int size = vector.size();
		for(int i=0;i<size;i++){
			System.out.println(vector.get(i) + " ");
		}		
	}
	
	
	
	public void readGraph(){	    	
		Map<String, Set<String>> graph = new HashMap<String, Set<String>>();			
		String line="",node1="",node2="";
		String[] vals = null;	
		Set<String> nodes = new HashSet<String>();		
		BufferedReader reader = null;								
		try {			
			reader = new BufferedReader(new FileReader(this.filename));						
			while ((line = reader.readLine()) != null) {				
				line = line.trim();
				vals = line.split("\t");
				node1= vals[0].trim();
				node2= vals[1].trim();								
				if(!graph.containsKey(node1)){
					nodes = new HashSet<String>();
					graph.put(node1, nodes);					
				}
				if(!graph.containsKey(node2)){
					nodes = new HashSet<String>();
					graph.put(node2, nodes);					
				}								
				nodes = graph.get(node1);
				nodes.add(node2);
				graph.put(node1, nodes);			
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		
		Set<String> keySet = graph.keySet();		
		this.N = keySet.size();			
		transMatrix = new SparseMatrix(N);		
		SparseVector outDegree = new SparseVector(N);		
		this.danglings = new SparseVector(N);
		this.w = new SparseVector(N);
				
		double val=0;
		
		for(String key:keySet){
			nodes = graph.get(key);			
			int i=Integer.parseInt(key);
			outDegree.put(i, nodes.size());									
			for(String dest:nodes){
				int j=Integer.parseInt(dest);
				val = transMatrix.get(j, i);
				transMatrix.put(j, i, (double)(val+1));				
			}
		}		
	
		double tmp=(double)1/N;
		double deg=0;
		
		for (int i = 0; i < N; i++)  {				
			w.put(i, tmp);
			deg = outDegree.get(i);
			
			if(deg!=0){
				for(int j=0;j<N;j++){
					val=transMatrix.get(j, i);
					val=(double)val/deg;
					transMatrix.put(j, i, val);
				}
			} else {
				danglings.put(i, (double)1.0);
			}						
		}
    }
	
	
	
	
	public void readGraph2(){	    	
		Map<String, Set<String>> graph = new HashMap<String, Set<String>>();			
		String line="",node1="",node2="";
		String[] vals = null;	
		String[] pair = null;
		Set<String> nodes = new HashSet<String>();		
		BufferedReader reader = null;
		
		try {			
			reader = new BufferedReader(new FileReader(this.filename));						
			while ((line = reader.readLine()) != null) {				
				vals = line.split(",");				
				for(String pairs:vals){
					pair = pairs.split("#");					
					node1= pair[0].trim();
					node2= pair[1].trim();			
					if(!graph.containsKey(node1)){
						nodes = new HashSet<String>();
						graph.put(node1, nodes);						
					}
					if(!graph.containsKey(node2)){
						nodes = new HashSet<String>();
						graph.put(node2, nodes);					
					}								
					nodes = graph.get(node1);
					nodes.add(node2);
					graph.put(node1, nodes);									
				}					
			}
			reader.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		Set<String> keySet = graph.keySet();	
		
		transMatrix = new SparseMatrix(N);		
		SparseVector outDegree = new SparseVector(N);		
		this.danglings = new SparseVector(N);
		this.w = new SparseVector(N);
				
		double val=0;
				
		for(String key:keySet){
			nodes = graph.get(key);		
			if(!nodes.isEmpty()){				
				int i=Integer.parseInt(key);						
				outDegree.put(i, nodes.size());				
				for(String dest:nodes){					
					int j=Integer.parseInt(dest);	
					val = transMatrix.get(j, i);
					transMatrix.put(j, i, val+1);				
				}				
			}		
		}		
	
		double tmp=(double)1/N;		
		double deg=0;
			
		for (int i = 0; i < N; i++)  {				
			w.put(i, tmp);
			deg = outDegree.get(i);			
			if(deg!=0){
				for(int j=0;j<N;j++){
					val=transMatrix.get(j, i);
					val=(double)val/deg;
					transMatrix.put(j, i, val);			
				}
			} else {
				danglings.put(i, (double)1.0);
			}	
		}		
    }
	
	
	
	
	
	
	public void readGraph3(Graph graph){	    	
		Map<Integer, Set<Integer>> outlinks = new HashMap<Integer, Set<Integer>>();		
		Set<Integer> nodes = new HashSet<Integer>();			
		Set<Integer> keySet = outlinks.keySet();
		
		outlinks = graph.getOutLinks();	
		
		this.N = graph.numNodes();		
		transMatrix = new SparseMatrix(N);		
		SparseVector outDegree = new SparseVector(N);		
		this.danglings = new SparseVector(N);
		this.w = new SparseVector(N);
				
		double val=0;
		
		for(Integer key:keySet){
			nodes = outlinks.get(key);		
			if(!nodes.isEmpty()){				
				int i=key;//Integer.parseInt(key);						
				outDegree.put(key, nodes.size());				
				for(Integer dest:nodes){					
					int j=dest;//Integer.parseInt(dest);	
					transMatrix.put(j, i, 1.0);				
				}				
			}		
		}		
			
		double tmp=(double)1/N;		
		double deg=0;
		
		for (int i = 0; i < N; i++)  {				
			w.put(i, tmp);
			deg = outDegree.get(i);			
			if(deg!=0){
				for(int j=0;j<N;j++){
					val=transMatrix.get(j, i);
					val=(double)val/deg;
					transMatrix.put(j, i, val);			
				}
			} else {
				danglings.put(i, (double)1.0);
			}	
		}		
    }	
}
