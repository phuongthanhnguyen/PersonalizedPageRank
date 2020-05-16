package it.poliba.sisinflab.PageRank;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Sets;


class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}



public class Runner {
	
	private String srcDir;
	private String groundTruth;
	private String simDir;
	private String recDir;
	private String resDir;
	private String subFolder;
	
	
	public Runner(){
		
	}
	
	public void loadConfigurations(){		
		Properties prop = new Properties();				
		try {
			prop.load(new FileInputStream("evaluation.properties"));		
			this.srcDir=prop.getProperty("sourceDirectory");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return;
	}
			
	
	public void run(){		
		System.out.println("CrossRec: Recommender System!");
		loadConfigurations();
		
		int step = 520;
		int numOfProjects = 5200;
						
		System.out.println(System.currentTimeMillis());
		
		for(int i=0;i<10;i++) {
			
			int trainingStartPos1 = 1;			
			int trainingEndPos1 = i*step;			
			int trainingStartPos2 = (i+1)*step+1;
			int trainingEndPos2 = numOfProjects;
			int testingStartPos = 1+i*step;
			int testingEndPos =   (i+1)*step;
			
			int k=i+1;
			subFolder = "Round" + Integer.toString(k);
						
			System.out.println(subFolder);
						
			this.groundTruth = this.srcDir + subFolder + "/" + "GroundTruth" + "/";
			this.recDir = this.srcDir + subFolder + "/" + "Recommendations" + "/";
			this.resDir = this.srcDir + subFolder + "/" + "Results" + "/";
			this.simDir = this.srcDir + subFolder + "/";
			
//			ComputeWeightCosineSimilarity(trainingStartPos1,trainingEndPos1,trainingStartPos2,trainingEndPos2,testingStartPos,testingEndPos);	

			RecommendationEngine engine = new RecommendationEngine(this.srcDir,this.subFolder,
					trainingStartPos1,
					trainingEndPos1,
					trainingStartPos2,
					trainingEndPos2,
					testingStartPos,
					testingEndPos);
			
		    int numberOfNeighbours = 10;							
	    
			engine.ItemBasedRecommendation(testingStartPos, testingEndPos, numberOfNeighbours);
					   		    
//			engine.UserBasedRecommendation(testingStartPos, testingEndPos, numberOfNeighbours);
							
		}
		
		System.out.println(System.currentTimeMillis());				
			
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*get the similarity matrix from the original RepoPal dataset*/
	
	public void ExtractingRepoPalSimilarityMatrix(int testingStartPos, int testingEndPos){
		
		DataReader reader = new DataReader();
			
		Map<Integer,String> testingProjects = new HashMap<Integer,String>();
		testingProjects = reader.readProjectList(this.srcDir + "projects.txt",testingStartPos,testingEndPos);
			
		Set<Integer> keyTestingProjects = testingProjects.keySet();
						
		String testingPro = "";
		
		Set<String> tests = new HashSet<String>();
			
		
		for(Integer keyTesting:keyTestingProjects){
			System.out.println(testingProjects.get(keyTesting));
			tests.add(testingProjects.get(keyTesting));
		}
		
		System.out.println("size: " + tests.size());
		
//		Set<String> values = (Set<String>) testingProjects.values();
		
		for(Integer keyTesting:keyTestingProjects){
			
			testingPro = testingProjects.get(keyTesting);		
			String filename = testingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");		
			String tmp = this.simDir + filename;					
			/*This is only for RepoPal*/
			
			tmp = tmp.replace("__", "_");
			
			String tmp2 = this.simDir + "Similarities" + "/" + filename;						
			String line = null;				
					
			String[] vals = null;
			
			try {
				BufferedReader bufread = new BufferedReader(new FileReader(tmp));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tmp2));				
				while ((line = bufread.readLine()) != null) {					
					vals = line.split("\t");							
					String secondProj = vals[1].trim().replace("git://github.com/", "").replace(".git", "");
								
					if(!tests.contains(secondProj)) {										
						writer.append(line);							
						writer.newLine();
						writer.flush();					
					}				
				}
				writer.close();
				bufread.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}		
	}
	
	
	
	
	
	
	
	
	
	
	
	public void ComputePageRankSimilarity(int trainingStartPos1, int trainingEndPos1, 
			int trainingStartPos2, int trainingEndPos2, 
			int testingStartPos, int testingEndPos){
				
		DataReader reader = new DataReader();
		
		Map<Integer, String> addedDictionary = new HashMap<Integer, String>();					
		String addedGraphFilename = "", addedDictFilename = "";
						
		Map<Integer,String> trainingProjects = reader.readProjectList2(this.srcDir + "projects.txt",trainingStartPos1,trainingEndPos1);
		
		if(trainingStartPos2!=0 && trainingEndPos2!=0) {
			Map<Integer,String> tempoProjects = reader.readProjectList2(this.srcDir + "projects.txt",trainingStartPos2,trainingEndPos2);		
			trainingProjects.putAll(tempoProjects);
		}
					
		Map<Integer,String> testingProjects = new HashMap<Integer,String>();
		testingProjects = reader.readProjectList2(this.srcDir + "projects.txt",testingStartPos,testingEndPos);
						
		Set<Integer> keyTrainingProjects = trainingProjects.keySet();
		Set<Integer> keyTestingProjects = testingProjects.keySet();
						
		String trainingPro = "", testingPro = "";
		String filename = "";
				
		Graph graph = null, addedGraph = null;		
		graph = new Graph();	
					
		for(Integer keyTraining:keyTrainingProjects){
			
			addedDictFilename="";		
			trainingPro = trainingProjects.get(keyTraining);						
			filename = trainingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");									
			addedGraphFilename = this.srcDir  +"graph_" + filename;			
			addedDictFilename = this.srcDir +"dicth_" + filename;			
					
			addedDictionary = reader.readDictionary2(addedDictFilename);						
			addedGraph = new Graph(addedGraphFilename,addedDictionary);
						
			if(graph==null) {
				graph = new Graph(addedGraph);				
			} else {				
				graph.combine(addedGraph, addedDictionary);				
			}				
		}		
				
		boolean getAlsoUsers = true;
		
		for(Integer keyTesting:keyTestingProjects){
			try {					
		
				Graph combinedGraph = new Graph(graph);				
				Map<String, Double> sim = new HashMap<String, Double>();
			
				testingPro = testingProjects.get(keyTesting);			
				filename = testingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
				addedGraphFilename = this.srcDir +"graph_" + filename;			
				addedDictFilename = this.srcDir +"dicth_" + filename;					
												
				addedDictionary = reader.extractHalfDictionary(addedDictFilename,this.groundTruth,getAlsoUsers);								
				addedGraph = new Graph(addedGraphFilename,addedDictionary);				
				combinedGraph.combine(addedGraph, addedDictionary);	

				System.out.println("Similarity computation for " + testingPro);
								
				PersonalizedPageRank ppr = null;
				SparseVector pageRankVector1 = null,pageRankVector2 = null;				
				ppr = new PersonalizedPageRank(combinedGraph);					
				
				int id1=0,id2=0;
				String repo1="", repo2="";				
					
				repo1 = "git://github.com/"+testingPro+".git";				
				id1 = combinedGraph.getDictionary().get(repo1);
				
				pageRankVector1 = ppr.calculatePersonalizedPageRankVector(id1);
												
				for(Integer keyTraining:keyTrainingProjects){					
					trainingPro = trainingProjects.get(keyTraining);
					repo2="git://github.com/"+trainingPro+".git";
					
					if(combinedGraph.getDictionary().containsKey(repo2)){						
						id2 = combinedGraph.getDictionary().get(repo2);						
						pageRankVector2 = ppr.calculatePersonalizedPageRankVector(id2);
						double val = pageRankVector1.cosineSimilarity(pageRankVector2);											
						sim.put(keyTraining.toString(),val);																
					}									
				}				
			
				ValueComparator bvc =  new ValueComparator(sim);        
				TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
				sorted_map.putAll(sim);				
				Set<String> keySet2 = sorted_map.keySet();				
								
				BufferedWriter writer = new BufferedWriter(new FileWriter(this.simDir+filename));												
				for(String key:keySet2){				
					String content = testingPro + "\t" + trainingProjects.get(Integer.parseInt(key)) + "\t" + sim.get(key);					
					writer.append(content);							
					writer.newLine();
					writer.flush();					
				}				
				writer.close();							
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
							
		return;
	}
	
	
	
	
	
	
	
	
	/*Compute similarity between every testing project and all training projects using Cosine Similarity with Weight*/
	
	public void ComputeWeightCosineSimilarity(int trainingStartPos1, int trainingEndPos1, 
			int trainingStartPos2, int trainingEndPos2, 
			int testingStartPos, int testingEndPos){
				
		DataReader reader = new DataReader();			
						
		Map<Integer,String> trainingProjects = new HashMap<Integer,String>();
		
		if(trainingStartPos1<trainingEndPos1) trainingProjects = reader.readProjectList(this.srcDir + "projects.txt",trainingStartPos1,trainingEndPos1);
		
		if(trainingStartPos2 < trainingEndPos2) {
			Map<Integer,String> tempoProjects = reader.readProjectList(this.srcDir + "projects.txt",trainingStartPos2,trainingEndPos2);		
			trainingProjects.putAll(tempoProjects);
		}
			
		Map<Integer,String> testingProjects = new HashMap<Integer,String>();
		testingProjects = reader.readProjectList(this.srcDir + "projects.txt",testingStartPos,testingEndPos);
						
		Set<Integer> keyTrainingProjects = trainingProjects.keySet();
		Set<Integer> keyTestingProjects = testingProjects.keySet();
						
		String trainingPro = "", testingPro = "", content = "";
		String testingFilename = "";
		String trainingFilename = "";
			
		Set<String> testingLibs = null;
		Set<String> trainingLibs = null;	
		Set<String> allTrainingLibs = null;
		Set<String> allLibs = null;
						
		Graph graph = null, trainingGraph = null, testingGraph = null;		
		Map<Integer, String> trainingDictionary = new HashMap<Integer, String>();
		Map<Integer, String> testingDictionary = new HashMap<Integer, String>();
				
		
		graph = new Graph();			
		String trainingDictFilename = "", trainingGraphFilename ="",filename="";
		allLibs = new HashSet<String>();
		allTrainingLibs = new HashSet<String>();
				
		Map<Integer, String> tmpDict = new HashMap<Integer, String>();	
		
		for(Integer keyTraining:keyTrainingProjects){		
			trainingPro = trainingProjects.get(keyTraining);			
			trainingFilename = trainingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
			trainingGraphFilename = this.srcDir  +"graph_" + trainingFilename;			
			trainingDictFilename = this.srcDir +"dicth_" + trainingFilename;		
			
			/*read all libraries for each project*/
			trainingLibs = reader.getLibraries(trainingDictFilename);	
				
			allTrainingLibs.addAll(trainingLibs);
				
			/*readDictionary4: read only libraries, ignore stars*/
			trainingDictionary = reader.readDictionary4(trainingDictFilename);						
			trainingGraph = new Graph(trainingGraphFilename,trainingDictionary);
						
			if(graph==null) {
				graph = new Graph(trainingGraph);				
			} else {				
				graph.combine(trainingGraph, trainingDictionary);				
			}				
		}
					
		/*getAlsoUsers = true if you want to get star events*/
		
		boolean getAlsoUsers = false;
		String testingGraphFilename="", testingDictFilename="";
				
		Map<Integer, Double> libWeight = new HashMap<Integer, Double>();		
		Map<Integer,Set<Integer>> graphEdges = null;
		
		Set<Integer> outlinks = new HashSet<Integer>();			
		Set<Integer> keySet = null;
		Map<String, Integer> combinedDictionary = null;
			
				
		for(Integer keyTesting:keyTestingProjects){
			try {		
				/*reset the buffer*/
				allLibs = new HashSet<String>();
				/*add all libraries from the training set*/
				allLibs.addAll(allTrainingLibs);							
				Graph combinedGraph = new Graph(graph);				
				Map<String, Double> sim = new HashMap<String, Double>();			
				testingPro = testingProjects.get(keyTesting);							
				filename = testingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
				testingFilename = testingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
				testingGraphFilename = this.srcDir +"graph_" + testingFilename;			
				testingDictFilename = this.srcDir +"dicth_" + testingFilename;
								
				testingLibs = new HashSet<String>();			
				testingLibs = reader.getHalfOfLibraries(testingDictFilename);
				
				allLibs.addAll(testingLibs);
				
				testingDictionary = reader.extractHalfDictionary(testingDictFilename,this.groundTruth,getAlsoUsers);								
				testingGraph = new Graph(testingGraphFilename,testingDictionary);				
				combinedGraph.combine(testingGraph, testingDictionary);
				combinedDictionary=combinedGraph.getDictionary();
				
//				System.out.println("Similarity computation for " + testingPro);
								
				graphEdges = combinedGraph.getOutLinks();
				keySet = graphEdges.keySet();
										
				/*explore the graph and count the number of occurrences of each library*/
				/*start nodes are projects and end nodes are libraries*/
				
				double freq = 0;
				for(Integer startNode:keySet){					
					outlinks = graphEdges.get(startNode);					
					for(Integer endNode:outlinks){						
						if(libWeight.containsKey(endNode)) {
							freq = libWeight.get(endNode)+1;												
						} else freq = 1;
						libWeight.put(endNode, freq);					
					}				
				}
				
				/*get the number of projects in the whole graph*/
				int numberOfProjects = keySet.size();				
				keySet = libWeight.keySet();				
				double weight = 0, idf = 0;								
				for(Integer libID:keySet){
					freq = libWeight.get(libID);
					weight = (double)numberOfProjects/freq;
					idf = Math.log(weight);
					libWeight.put(libID, idf);									
				}
				
				
				for(Integer keyTraining:keyTrainingProjects){									
					trainingPro = trainingProjects.get(keyTraining);							
					trainingFilename = trainingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
					
					trainingDictFilename = this.srcDir +"dicth_" + trainingFilename;					
					/*read all libraries for each project*/
					trainingLibs = reader.getLibraries(trainingDictFilename);
									
					Set<String> union = Sets.union(testingLibs, trainingLibs);				
					List<String> libSet = new ArrayList<String>();
					
					/*change the set to an ordered list*/
					for(String lib:union)libSet.add(lib);
					int size = union.size();
					if(size!=libSet.size())System.out.println("Something went wrong!");
									
					/*Using Cosine Similarity*/
					double vector1[] = new double[size];
					double vector2[] = new double[size];
					double val=0;
										
					for(int i=0;i<size;i++) {	
						String lib = libSet.get(i);
						if(testingLibs.contains(lib)) {
							int libID = combinedDictionary.get(lib);
							vector1[i]=libWeight.get(libID);
						}
						else vector1[i]=0;
						
						if(trainingLibs.contains(lib)) {
							int libID = combinedDictionary.get(lib);
							vector2[i]=libWeight.get(libID);
						}
						else vector2[i]=0;	
//						System.out.println(vector1[i] + ":" + vector2[i]);
					}					
					val = CosineSimilarity(vector1,vector2);					
					sim.put(keyTraining.toString(),val);				
				}															
						
				
				ValueComparator bvc =  new ValueComparator(sim);        
				TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
				sorted_map.putAll(sim);				
				Set<String> keySet2 = sorted_map.keySet();				
								
				BufferedWriter writer = new BufferedWriter(new FileWriter(this.simDir+filename));												
				for(String key:keySet2){				
					content = testingPro + "\t" + trainingProjects.get(Integer.parseInt(key)) + "\t" + sim.get(key);					
					writer.append(content);							
					writer.newLine();
					writer.flush();					
				}				
				writer.close();							
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*Compute similarity every pair of projects, using a common graph, and a common dictionary*/
	
	public void ComputeProjectSimilarity(int startPos, int endPos){
				
		DataReader reader = new DataReader();						
		Map<Integer,String> Projects = reader.readProjectList(this.srcDir + "projects.txt",startPos,endPos);					
		Set<Integer> keyProjects = Projects.keySet();
		
		String Project = "", content = "";
	
		Set<String> allLibs = null;
						
		Graph graph = null;		
		Map<Integer, String> Dictionary = new HashMap<Integer, String>();
				
		graph = new Graph();			
		String dictFilename = "", graphFilename ="",filename="";
		allLibs = new HashSet<String>();
								
		for(Integer key:keyProjects){		
			Project = Projects.get(key);			
			filename = Project.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
			graphFilename = this.srcDir  +"graph_" + filename;			
			dictFilename = this.srcDir +"dicth_" + filename;		
			
			/*read all libraries for each project*/
			Set<String> myLibs = reader.getLibraries(dictFilename);	
				
			allLibs.addAll(myLibs);
			
						
			/*readDictionary4: read only libraries, ignore stars*/
			Map<Integer, String> myDictionary = reader.readDictionary4(dictFilename);						
			Graph myGraph = new Graph(graphFilename,myDictionary);
						
			if(graph==null) {
				graph = new Graph(myGraph);				
			} else {				
				graph.combine(myGraph, myDictionary);				
			}				
		}

			
		Map<Integer, Double> libWeight = new HashMap<Integer, Double>();		
		Map<Integer,Set<Integer>> graphEdges = null;
		
		Set<Integer> outlinks = new HashSet<Integer>();			
		Set<Integer> keySet = null;
		Map<String, Integer> combinedDictionary = null;
			
		
		graphEdges = graph.getOutLinks();
		keySet = graphEdges.keySet();
		combinedDictionary=graph.getDictionary();
		
		double freq = 0;
		for(Integer startNode:keySet){					
			outlinks = graphEdges.get(startNode);					
			for(Integer endNode:outlinks){						
				if(libWeight.containsKey(endNode)) {
					freq = libWeight.get(endNode)+1;												
				} else freq = 1;
				libWeight.put(endNode, freq);					
			}				
		}
		
		/*get the number of projects in the whole graph*/
		int numberOfProjects = keySet.size();
		
		keySet = libWeight.keySet();
		
		double weight = 0, idf = 0;
						
		for(Integer libID:keySet){
			freq = libWeight.get(libID);
			weight = (double)numberOfProjects/freq;
			idf = Math.log(weight);
			libWeight.put(libID, idf);									
		}
		
		List<String> libSet = new ArrayList<String>();
		
		/*change the set to an ordered list*/
		for(String lib:allLibs)libSet.add(lib);
		int size = allLibs.size();
		if(size!=libSet.size())System.out.println("Something went wrong!");
	
		
		for(Integer key1:keyProjects){
			try {	
				String Project1 = Projects.get(key1);	
				System.out.println("Similarity computation for " + Project1);
				Map<String, Double> sim = new HashMap<String, Double>();						
				String filename1 = Project1.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
				graphFilename = this.srcDir  +"graph_" + filename1;			
				dictFilename = this.srcDir +"dicth_" + filename1;		
				
				/*read all libraries for each project*/
				Set<String> myLibs = reader.getLibraries(dictFilename);	
				
				double vector1[] = new double[size];
				
				for(int i=0;i<size;i++) {	
					String lib = libSet.get(i);
					if(myLibs.contains(lib)) {
						int libID = combinedDictionary.get(lib);
						vector1[i]=libWeight.get(libID);
					}
					else vector1[i]=0;				
				}	
													
				/*Using Cosine Similarity*/
				
				double vector2[] = new double[size];
				double val=0;
								
				for(Integer key2:keyProjects){
					
					if(!key2.equals(key1)) {
					
						String Project2 = Projects.get(key2);							
						String filename2 = Project2.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
						graphFilename = this.srcDir  +"graph_" + filename2;			
						dictFilename = this.srcDir +"dicth_" + filename2;					
						/*read all libraries for each project*/
						myLibs = reader.getLibraries(dictFilename);
						
						for(int i=0;i<size;i++) {	
							String lib = libSet.get(i);
							if(myLibs.contains(lib)) {
								int libID = combinedDictionary.get(lib);
								vector2[i]=libWeight.get(libID);
							}
							else vector2[i]=0;				
						}					
						val = CosineSimilarity(vector1,vector2);					
						sim.put(key2.toString(),val);
					}				
				}															
									
				ValueComparator bvc =  new ValueComparator(sim);        
				TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
				sorted_map.putAll(sim);				
				Set<String> keySet2 = sorted_map.keySet();				
								
				BufferedWriter writer = new BufferedWriter(new FileWriter(this.simDir+filename1));												
				for(String key:keySet2){				
					content = Project1 + "\t" + Projects.get(Integer.parseInt(key)) + "\t" + sim.get(key);					
					writer.append(content);							
					writer.newLine();
					writer.flush();					
				}				
				writer.close();							
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	
		return;
	}
	
	
	
	
	
	
	
   /*Compute similarity between every testing project and all training projects using Cosine Similarity with PageRank Weight*/
	
	public void ComputePageRankWeightCosineSimilarity(int trainingStartPos1, int trainingEndPos1, 
			int trainingStartPos2, int trainingEndPos2, 
			int testingStartPos, int testingEndPos){
				
		DataReader reader = new DataReader();			
						
		Map<Integer,String> trainingProjects = reader.readProjectList(this.srcDir + "projects.txt",trainingStartPos1,trainingEndPos1);
		
		if(trainingStartPos2!=0 && trainingEndPos2!=0) {
			Map<Integer,String> tempoProjects = reader.readProjectList(this.srcDir + "projects.txt",trainingStartPos2,trainingEndPos2);		
			trainingProjects.putAll(tempoProjects);
		}
			
		Map<Integer,String> testingProjects = new HashMap<Integer,String>();
		testingProjects = reader.readProjectList(this.srcDir + "projects.txt",testingStartPos,testingEndPos);
						
		Set<Integer> keyTrainingProjects = trainingProjects.keySet();
		Set<Integer> keyTestingProjects = testingProjects.keySet();
						
		String trainingPro = "", testingPro = "", content = "";
		String testingFilename = "";
		String trainingFilename = "";
			
		Set<String> testingLibs = null;
		Set<String> trainingLibs = null;	
		Set<String> allTrainingLibs = null;
		Set<String> allLibs = null;
						
		Graph graph = null, trainingGraph = null, testingGraph = null;		
		Map<Integer, String> trainingDictionary = new HashMap<Integer, String>();
		Map<Integer, String> testingDictionary = new HashMap<Integer, String>();
				
		
		graph = new Graph();			
		String trainingDictFilename = "", trainingGraphFilename ="",filename="";
		allLibs = new HashSet<String>();
		allTrainingLibs = new HashSet<String>();
				
		Map<Integer, String> tmpDict = new HashMap<Integer, String>();	
		
		for(Integer keyTraining:keyTrainingProjects){		
			trainingPro = trainingProjects.get(keyTraining);			
			trainingFilename = trainingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
			trainingGraphFilename = this.srcDir  +"graph_" + trainingFilename;			
			trainingDictFilename = this.srcDir +"dicth_" + trainingFilename;		
			
			/*read all libraries for each project*/
			trainingLibs = reader.getLibraries(trainingDictFilename);	
				
			allTrainingLibs.addAll(trainingLibs);
				
			/*readDictionary4: read only libraries, ignore stars*/
			trainingDictionary = reader.readDictionary4(trainingDictFilename);						
			trainingGraph = new Graph(trainingGraphFilename,trainingDictionary);
						
			if(graph==null) {
				graph = new Graph(trainingGraph);				
			} else {				
				graph.combine(trainingGraph, trainingDictionary);				
			}				
		}
					
		/*getAlsoUsers = true if you want to get star events*/
		
		boolean getAlsoUsers = false;
		String testingGraphFilename="", testingDictFilename="";
				
		Map<Integer, Double> libWeight = new HashMap<Integer, Double>();		
		Map<Integer,Set<Integer>> graphEdges = null;
		
		Set<Integer> outlinks = new HashSet<Integer>();			
		Set<Integer> keySet = null;
		Map<String, Integer> combinedDictionary = null;
			
				
		for(Integer keyTesting:keyTestingProjects){
			try {		
				/*reset the buffer*/
				allLibs = new HashSet<String>();
				/*add all libraries from the training set*/
				allLibs.addAll(allTrainingLibs);							
				Graph combinedGraph = new Graph(graph);				
				Map<String, Double> sim = new HashMap<String, Double>();			
				testingPro = testingProjects.get(keyTesting);							
				filename = testingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
				testingFilename = testingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
				testingGraphFilename = this.srcDir +"graph_" + testingFilename;			
				testingDictFilename = this.srcDir +"dicth_" + testingFilename;
								
				testingLibs = new HashSet<String>();			
				testingLibs = reader.getHalfOfLibraries(testingDictFilename);
				
				allLibs.addAll(testingLibs);
				
				testingDictionary = reader.extractHalfDictionary(testingDictFilename,this.groundTruth,getAlsoUsers);								
				testingGraph = new Graph(testingGraphFilename,testingDictionary);				
				combinedGraph.combine(testingGraph, testingDictionary);
				combinedDictionary=combinedGraph.getDictionary();
				
				
				
				System.out.println("Similarity computation for " + testingPro);
				
				
				
				PersonalizedPageRank ppr = null;							
				ppr = new PersonalizedPageRank(combinedGraph);
				SparseVector pageRankVector = ppr.calculatePersonalizedPageRankVector();
							
				
				graphEdges = combinedGraph.getOutLinks();
				keySet = graphEdges.keySet();
										
				/*explore the graph and count the number of occurrences of each library*/
				/*start nodes are projects and end nodes are libraries*/
				
				
				
				int length = pageRankVector.size();
				
				for(int i=0;i<length;i++)libWeight.put(i, pageRankVector.get(i));

				
//				double freq = 0;
//				for(Integer startNode:keySet){					
//					outlinks = graphEdges.get(startNode);					
//					for(Integer endNode:outlinks){						
//						if(libWeight.containsKey(endNode)) {
//							freq = libWeight.get(endNode)+1;												
//						} else freq = 1;
//						libWeight.put(endNode, freq);					
//					}				
//				}
				
				
				
				/*get the number of projects in the whole graph*/
				int numberOfProjects = keySet.size();
				
//				keySet = libWeight.keySet();
//				
//				double weight = 0, idf = 0;
//								
//				for(Integer libID:keySet){
//					freq = libWeight.get(libID);
//					weight = (double)numberOfProjects/freq;
//					idf = Math.log(weight);
//					libWeight.put(libID, idf);									
//				}
				
				
				
				List<String> libSet = new ArrayList<String>();
				
				/*change the set to an ordered list*/
				for(String lib:allLibs)libSet.add(lib);
				int size = allLibs.size();
				if(size!=libSet.size())System.out.println("Something went wrong!");
									
				/*Using Cosine Similarity*/
				double vector1[] = new double[size];
				double vector2[] = new double[size];
				double val=0;
							
				for(int i=0;i<size;i++) {	
					String lib = libSet.get(i);
					if(testingLibs.contains(lib)) {
						int libID = combinedDictionary.get(lib);
						vector1[i]=libWeight.get(libID);
					}
					else vector1[i]=0;				
				}				
				
				for(Integer keyTraining:keyTrainingProjects){									
					trainingPro = trainingProjects.get(keyTraining);							
					trainingFilename = trainingPro.replace("git://github.com/", "").replace(".git", "").replace("/", "__");			
					trainingGraphFilename = this.srcDir  +"graph_" + trainingFilename;			
					trainingDictFilename = this.srcDir +"dicth_" + trainingFilename;					
					/*read all libraries for each project*/
					trainingLibs = reader.getLibraries(trainingDictFilename);
					
					for(int i=0;i<size;i++) {	
						String lib = libSet.get(i);
						if(trainingLibs.contains(lib)) {
							int libID = combinedDictionary.get(lib);
							vector2[i]=libWeight.get(libID);
						}
						else vector2[i]=0;				
					}
					
					val = CosineSimilarity(vector1,vector2);					
					sim.put(keyTraining.toString(),val);				
				}															
						
				
				ValueComparator bvc =  new ValueComparator(sim);        
				TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
				sorted_map.putAll(sim);				
				Set<String> keySet2 = sorted_map.keySet();				
								
				BufferedWriter writer = new BufferedWriter(new FileWriter(this.simDir+filename));												
				for(String key:keySet2){				
					content = testingPro + "\t" + trainingProjects.get(Integer.parseInt(key)) + "\t" + sim.get(key);					
					writer.append(content);							
					writer.newLine();
					writer.flush();					
				}				
				writer.close();							
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		return;
	}

		
		
	
	public double CosineSimilarity(double[] vector1, double[] vector2) {        
        double sclar = 0, norm1 = 0, norm2 = 0;
        int length = vector1.length;       
        for(int i=0;i<length;i++) sclar+=vector1[i]*vector2[i];
        for(int i=0;i<length;i++) norm1+=vector1[i]*vector1[i];
        for(int i=0;i<length;i++) norm2+=vector2[i]*vector2[i];
        double ret = 0;
        double norm = norm1*norm2;
        ret = (double)sclar / Math.sqrt(norm);        
        return ret;
    }
	
	
	
	
	public static void main(String[] args) {	
		Runner runner = new Runner();			
		runner.run();				    		    
		return;
	}	
	
}
