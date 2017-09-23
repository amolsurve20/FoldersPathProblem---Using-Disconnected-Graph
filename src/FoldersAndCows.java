import java.util.*;
public class FoldersAndCows 
{
	public static void main(String args[])
	{
		// Create a graph object
		Graph graph = new Graph();
		
		// Create a graph based on standard input given
		graph = createGraph();
		
		// Get the map of each folder id as key and HashSet of cow ids accessing that folder as values
		HashMap<Integer, HashSet<Integer>> result = createMap(graph);

		// Identify the leaf nodes in the graph
		HashSet<Folder> leaves = getLeaves(graph.getNodes());
		
		// Create a HashSet of leaf folder IDs
		HashSet<Integer> leafFolders = new HashSet<Integer>();
		for(Folder each : leaves)
		{
			leafFolders.add(each.getFolderId());
		}
		
		// Slice the result of the graph traversal to store just the leaf folders and cows having access to leaves
		result.keySet().retainAll(leafFolders);

		// Get the total number of cows present
		int numberOfCows = graph.getCows();
		
		// HashSet to store uncool cows
		HashSet<Integer> uncoolCows = new HashSet<Integer>();
		
		for(Map.Entry<Integer, HashSet<Integer>>entry : result.entrySet())
		{
			//System.out.println(entry.getKey());
			//System.out.println(entry.getValue());
			HashSet<Integer> cowList = entry.getValue();
			
			for(int i=0; i<numberOfCows; i++)
			{
				if(!cowList.contains(i))
				{
					uncoolCows.add(i);
				}
			}
			
			if(uncoolCows.size() == numberOfCows)
				break;
		}
		
		for(Integer i: uncoolCows)
		{
			System.out.print(i + " ");
		}
			
	}
	
	// creating a HashMap of key as folders if and values as cow list based on shared vs confidential access constraints
	// w.r.t. parent child relationships between folders 
	public static HashMap<Integer, HashSet<Integer>> createMap(Graph graph)
	{
		HashMap<Integer, HashSet<Integer>> result = new HashMap<Integer, HashSet<Integer>>();
		ArrayList<Folder> folders = graph.getNodes();
		
		for(Folder each: folders)
		{
			if(!each.isVisited())
			{
				ArrayList<Integer> cowList = getCows(each);
				int folderId = each.getFolderId();
				result.put(folderId, new HashSet(cowList));
			}
		}
		
		return result;
	}
	
	public static ArrayList<Integer> getCows(Folder each)
	{
		if(each == null || each.isVisited())
			return null;
		
		ArrayList<Integer> cowList = each.getCowsList();
		each.setVisited();
		
		// if the folder is confidential, just return the cows having explicit access to the folder
		if(each.isConfidential())
		{
			return cowList;
		}
		// if the folder is shared, check if the parent exists 
		//and if it is also a shared folder, add the cows accessing parent folder to the sub-folder cow list
		else if(each.isShared())
			{
				Folder parent = each.getParent();
				// checking for both parent not null and parent folder is shared so that cows can access the child 
				// shared folder 
				if(parent != null && parent.isShared())
				{
					ArrayList<Integer> parentCows = parent.getCowsList();
					cowList.addAll(parentCows);
				}
			}
		
		return cowList;
	}
	
	// function to get the leaf folders
	public static HashSet<Folder> getLeaves(ArrayList<Folder>folders)
	{
		HashSet<Folder> leafNodes = new HashSet<Folder>();
		
		for(Folder folder: folders)
		{
			if(folder.getOutDegree() == 0)
			{
				leafNodes.add(folder);
			}
		}
		
		return leafNodes;
	}
	
	// function to read input from the console
	public static Queue<String[]> readInput()
	{
		String line;
		Scanner stdin = new Scanner(System.in);
		Queue<String []> input = new LinkedList<String[]>();
        while(stdin.hasNextLine() && !( line = stdin.nextLine() ).equals( "" ))
        {
            String[] tokens = line.split(" ");
            input.add(tokens);  
        }
        stdin.close();
        
        return input;
	}
	
	// function to build the graph based on the given node information and relationship between them
	public static Graph createGraph()
	{
        Graph graph = new Graph();
        Queue<String []> input = readInput();
        
        // Getting the number of cows
        String firstLine [] = input.poll();
        int q = Integer.parseInt(firstLine[0]);
        graph.setCowsNumber(q);
        
        // Getting the number of shared folders
        String secondLine [] = input.poll();
        int m = Integer.parseInt(secondLine[0]);
       
        // Getting the number of confidential folders
        int n = Integer.parseInt(secondLine[1]);

        for(int i=0; i<m; i++)
        {
        	String each [] = input.poll();
        	int sharedFolderId = Integer.parseInt(each[0]);
        	Folder newFolder = graph.getOrCreateNode(sharedFolderId);        	
        	newFolder.setAccessShared();
        	int numberOfCows = Integer.parseInt(each[1]);        	
        	for(int offset=0; offset<numberOfCows; offset++)
        	{
        		Cow cow = new Cow(Integer.parseInt(each[2 + offset]));
        		newFolder.addCow(cow);
        	}       	
        }

        for(int i=0; i<n; i++)
        {
        	String each [] = input.poll();
        	int confidentialFolderId = Integer.parseInt(each[0]);
        	Folder newFolder = graph.getOrCreateNode(confidentialFolderId);;
        	newFolder.setAccessConfidential();
        	int numberOfCows = Integer.parseInt(each[1]);
        	for(int offset=0; offset<numberOfCows; offset++)
        	{
        		Cow cow = new Cow(Integer.parseInt(each[2 + offset]));
        		newFolder.addCow(cow);
        	}
        }
        
        int g = Integer.parseInt(input.poll()[0]);
        for(int i=0; i<g; i++)
        {
        	String each[] = input.poll();
        	int from = Integer.parseInt(each[0]);
        	int to = Integer.parseInt(each[1]);
        	graph.addEdge(from, to);
        }
        
        return graph;
	}
}

/*
 *  Graph class binds the nodes and edges together to create a Graph structure 
 */
class Graph
{
	private ArrayList<Folder> nodes = new ArrayList<Folder>();
	private HashMap<Integer, Folder> map = new HashMap<Integer, Folder>();
	private int totalCows = 0;
	
	public void setCowsNumber(int numberOfCows)
	{
		this.totalCows = numberOfCows;
	}
	
	public int getCows()
	{
		return this.totalCows;
	}
	
	public Folder getOrCreateNode(int folderID)
	{
		if(!map.containsKey(folderID))
		{
			Folder node = new Folder(folderID);
			nodes.add(node);
			map.put(folderID, node);
		}
		
		return map.get(folderID);
	}
	
	public void addEdge(int from, int to)
	{
		Folder parent = getOrCreateNode(from);
		Folder subFolder = getOrCreateNode(to);
		
		parent.addChildrenFolder(subFolder);
	}
	
	public ArrayList<Folder>getNodes()
	{
		return nodes;
	}
}

/*
 * Cow can have it's own attributes and hence I have implemented it as a separate class called Cow
 */
class Cow
{
	public int id;
	
	public Cow(int id)
	{
		this.id = id;
	}
	
	// getting the cow id
	public int getCowId()
	{
		return this.id;
	}
	
}

/*
 * creating a class called Folder containing -
 * 1. folder id
 * 2. boolean variable to denote whether the folder is shared or not
 * 3. HashMap of sub-folder where key is sub-folder id and value is the sub-folder
 * 4. ArrayList of sub-folders
 * 5. ArrayList cow IDs accessing this folder
 * 6. inDegree of this node
 * 7. outDegree of this node
 * 8. parent Folder of this node (folder)
 * 9. boolean variable isVisited to indicate whether the folder (node) is visited or not during traversal
 */

class Folder
{
	private int id;
	private boolean isShared;
	private HashMap<Integer, Folder> subFoldersMap;
	private ArrayList<Integer> subFoldersList;
	private ArrayList<Integer> cowsList;
	private int inDegree;
	private int outDegree;
	private Folder parent;
	private boolean isVisited;
	
	public Folder(int id)
	{
		this.id = id;
		this.isShared = true;
		subFoldersMap = new HashMap<Integer, Folder>();
		subFoldersList = new ArrayList<Integer>();
		cowsList = new ArrayList<Integer>();
		inDegree = 0;
		outDegree = 0;
		parent = null;
		isVisited = false;
	}
	
	// getting folder id
	public int getFolderId()
	{
		return this.id;
	}
	
	// changing access to confidential
	public void setAccessConfidential()
	{
		this.isShared = false;
	}
	
	// changing access to shared
	public void setAccessShared()
	{
		this.isShared = true;
	}
	
	// identifying if the current access is confidential
	public boolean isConfidential()
	{
		return this.isShared == false;
	}
	
	// identifying if the current access is shared
	public boolean isShared()
	{
		return this.isShared == true;
	}
	
	public void addCow(Cow cow)
	{
		if(!cowsList.contains(cow.getCowId()))
		{
			cowsList.add(cow.getCowId());
		}
	}
	
	public ArrayList<Integer> getCowsList()
	{
		return this.cowsList;
	}
	
	// adding the children i.e. sub-folder
	public void addChildrenFolder(Folder folder)
	{
		if(!subFoldersMap.containsKey(folder.getFolderId()))
		{
			subFoldersMap.put(folder.getFolderId(), folder);
			subFoldersList.add(folder.getFolderId());
			folder.setParent(this);
			folder.incrementIndegree();
			this.incrementOutDegree();
		}
	}
	
	// getting the parent folder of this folder
	public Folder getParent()
	{
		return this.parent;
	}
	
	public void setParent(Folder parent)
	{
		this.parent = parent;
	}
	
	public int getIndegree()
	{
		return this.inDegree;
	}
	
	public int getOutDegree()
	{
		return this.outDegree;
	}
	
	public void incrementIndegree()
	{
		inDegree ++;
	}
	
	public void decrementIndegree()
	{
		inDegree --;
	}
	
	public void incrementOutDegree()
	{
		outDegree ++;
	}
	
	public void decrementOutDegree()
	{
		outDegree --;
	}
	
	public boolean isVisited()
	{
		return this.isVisited == true;
				
	}
	
	public boolean setVisited()
	{
		return this.isVisited == true;
	}
}