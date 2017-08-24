package name.dxliu.oracle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import name.dxliu.bean.IntegerEdge;

/**
* @author Daxin Liu
* 
* This class is a default implementation of the oracle. The methodology can be obtain in reference.
* 
*/
public class DefaultOracle extends AbstractOracle{
	
	private static final byte INF8=100;  
	private Vector<VVPair> es;// edge set.
	private int num_v_=0;
	public ArrayList<ArrayList<VDPair>> index=null;//oracle ,Integer represent vertex. Short represent distance.
	public static int MAXVERTEX;
	public static int MAX;	
	private SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
	
	/*
	 *	This mapping help to regular the vertex_id of a input graph. Make the graph's id be continuous(to fully exploit the 
	 *  integer range).
	 *	 
	 **/
	public int graph_map_fun[];//maxVertece;

	/*
	 * This mapping function map the original graph to a graph used by oracle(rename the vertex id and arrange them by vertex degree.
	 * The smallest vertex id will persist the most centered vertex (degree is the highest).
	 *
	 */
	public int[] rank;

	/*
	 * invere mapping of 'rank'.
	 */
	public int[] inv;

	
	//simple cache strategy used to accelerate the query.
	byte cache[][]=new byte[4][MAX];
	int cache_node[]=new int[4];
	int current_experd=0;
	public DefaultOracle() {}
	
	/**
	 * @param ifs an input stream of the graph file.
	 * @return whether the construction succeeded.
	 */
	public boolean ConstructIndex(InputStream ifs){		
		es=new Vector<VVPair>();
		BufferedReader reader=new BufferedReader(new InputStreamReader(ifs));
		String sr=null;
		try {
			while((sr=reader.readLine())!=null){
				String[] temp=sr.split(" +");
				int v=graph_map_fun[Integer.valueOf(temp[0])];
				int w=graph_map_fun[Integer.valueOf(temp[1])];
				es.add(new VVPair(v,w));				
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		//construct(es) now
		int E = es.size();
		
		int V = MAX;
		num_v_=V;
		System.out.println(E+" "+V);
		Random ran=new Random();
		Vector<Vector<Integer>> adj=new Vector<Vector<Integer>>(V);
		rank=new int[V];//mapping function.
		inv=new int [V];//inverse mapping function.
		
		Vector<DegVPair> deg =new Vector<DegVPair>(V);
		Vector<Vector<Integer>> new_adj=new Vector<Vector<Integer>>(V);//new adjacent table .
		index=new ArrayList<ArrayList<VDPair>>(V);
		
		for(int v=0;v<V;v++){
			adj.add(new Vector<Integer>());
			inv[v]=0;
			rank[v]=0;
			deg.add(new DegVPair());
			new_adj.add(new Vector<Integer>());			
			index.add(new ArrayList<VDPair>());				
		}//initialization
		
		for(int i=0;i<es.size();i++){
			 int v = es.get(i).first , w = es.get(i).second;			 
			 adj.get(v).add(w); 
			 adj.get(w).add(v); 
		}
				
		{			
			for (int v = 0; v < V; ++v) {
			      // We add a random value here to diffuse nearby vertices				
			      deg.set(v, new DegVPair((float)adj.get(v).size()+ran.nextFloat() , v)  );   //以（degree+float,vertex）产生对反正deg[v]数组中，foalt随机0-1,
			    }
			Collections.sort(deg,DegVPair.dvpCmp);//sort the vertex by degree.
			for (int i = 0; i < V; ++i){ 
				inv[i]=deg.get(i).vertexId;
				rank[inv[i]]=i;
			//	rank[i]=deg.get(i).second;
			} //Acturelly, vertex_id i has a degree rank rank[i].(ascending order).					
		    for (int v = 0; v < V; ++v) {
		        for (int i = 0; i < adj.get(v).size(); ++i) {
		          new_adj.get(rank[v]).add(rank[adj.get(v).get(i)]   );
		        }
		      }
		    adj=null;//free the memory.
		}		
		//pruning BFS;		
		byte P[]=new byte[V];
		for(int v=0;v<V;v++)
		{P[v]=(byte)INF8;}
		
		
		
		int stack[] =new int [MAX];
		int top=-1;		
		// queue.
		int queue[]=new int [MAX+1];
		int head=0,rear=0;
		 
		
		System.out.println(time.format(new Date()));
		
		// pruning BFS for V round.
		for(int v=0;v<V;v++){
			//System.out.println("this is the "+v+"round BFS");			
			top=-1;head=0;rear=0;
			
			queue[rear]=v;	rear=(rear+1)%(MAX+1);	
			P[v]=0;		stack[++top]=v;
			while(head!=rear){
				int u=queue[head];head=(head+1)%(MAX+1);// dequeue
				if(QueryDistance(v,u)<=P[u])continue;
				
				index.get(u).add( VDPair.getVDPair(v, P[u]) );//   new VDPair(v, P[u])   );
				
				for(int k=0;k<new_adj.get(u).size();k++ ){				
					int w = new_adj.get(u).get(k);
					if(P[w]==INF8){
						P[w]=(byte) (P[u]+1);   stack[++top]=w;
						queue[rear]=w;	rear=(rear+1)%(MAX+1);	
					}
				}
			}
			while(top!=-1)  P[stack[top--]]=(byte)INF8;
		}
		System.out.println(time.format(new Date()));
		 return true;
	}
	
	
	public boolean ConstructIndex(String filename){
		//load vertex mapping function. There are two mapping during the whole progress. The first one discards the singleton vertex.
		//and the second one sort the vertex via degree. Currently, process the first one.
		constructfirstMap(filename);//load graph_map_fun[] mapping.					
				
		InputStream is=null;
		try {
			is = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("failed to load required graph file");
		}		
		return is!=null&&ConstructIndex(is);
	}
	
	/**
	 * @param v vertexId
	 * @param w vertexId
	 * @return distance between v and w.
	 */
	private byte QueryDistance(int v, int w){//lable of index is sorted by vertex_id.
		byte distace=INF8;
		if(index==null)
			return INF8;
		ArrayList<VDPair> lable_v=index.get(v);
		ArrayList<VDPair> lable_w=index.get(w);		
		if(lable_v.size()==0||lable_w.size()==0) return INF8;
		{
			int i=0,j=0;
			while(i<lable_v.size()&&j<lable_w.size()){
				while(lable_v.get(i).first>lable_w.get(j).first) {j++;if(j>=lable_w.size())break;}
				if(j>=lable_w.size())break;
				if(lable_v.get(i).first==lable_w.get(j).first){
					byte temp_distance=(byte) (lable_v.get(i).second+lable_w.get(j).second);
					distace=distace>temp_distance?temp_distance:distace;
					i++;j++;
				}else i++;
			}			
		}
		return distace;		
	}
	
	/**
	 *  answering query from the outside. To obtain the inside distance, a mapping from outside vertex to inner vertex is needed.	
	 */
	public byte Query(int v,int target){
		int s=graph_map_fun[v],t=graph_map_fun[target];
		if(s==-1||t==-1) return 100; //singleton vertex.		
		return  QueryDistance(rank[s],rank[t]);	
	}	
	/**
	 * @param ifs oracle input file stream
	 * @return whether load the oracle successfully.
	 */
	public boolean LoadIndex(InputStream ifs){
			
		byte [] buffer=new byte[1000];//5B * 200
		ByteArrayInputStream bais=null;
		DataInputStream dis=null;
		int len=0;
		int in_i=0;   byte in_s = 0;
		index=new ArrayList<ArrayList<VDPair>>();
				
		ArrayList<VDPair> tobeAdded=new ArrayList<VDPair>();
		
		try{
			len=ifs.read(buffer);
			while(len!=-1){
				bais=new ByteArrayInputStream(buffer,0,len);
				dis=new DataInputStream(bais);
				if(len%5==0){					
					for(int i=0;i<(len/5);i++){
						in_i =	dis.readInt();
						in_s =  dis.readByte();
						if(in_i==-1&&in_s==-1){
							index.add(tobeAdded);
							tobeAdded=new ArrayList<VDPair>();
						}else
							tobeAdded.add( VDPair.getVDPair(in_i, in_s));// new VDPair(in_i, in_s));						
					}					
				}else {return false;}
				len=ifs.read(buffer);
			}
			bais.close();
			dis.close();
			ifs.close();
			buffer=null;
		}catch(Exception e){}
		num_v_=index.size();
		return true;		
	}
	
	public boolean LoadIndex(String filename,boolean forjar){
		
		InputStream is=null;
				try {
					
					//load inv[] and rank[]
					InputStream is_inv,is_rank,is_graphmap;
					if(forjar==false){
						is=new FileInputStream(new File(filename));
						is_inv=new FileInputStream(new File(filename+"_inv"));					
						is_rank=new FileInputStream(new File(filename+"_rank"));
						is_graphmap=new FileInputStream(new File(filename+"_graphmap"));						
					}else{
						is= this.getClass().getResourceAsStream(filename);
						is_inv=this.getClass().getResourceAsStream(filename+"_inv");
						is_rank=this.getClass().getResourceAsStream(filename+"_rank");
						is_graphmap= this.getClass().getResourceAsStream(filename+"_graphmap");
					}
					
					
					
					
					byte [] buffer=new byte[1024];
					ByteArrayInputStream bais=null;
					DataInputStream dis=null;
					int len=0;
					
					inv=new int[MAX];
					int inv_flag=0;
						len=is_inv.read(buffer);
					while(len!=-1){
						bais=new ByteArrayInputStream(buffer,0,len);
						dis=new DataInputStream(bais);
						for(int v=0;v<(len/4);v++){
							inv[inv_flag++]=dis.readInt();
						}len=is_inv.read(buffer);						
					}is_inv.close();
					bais.close();
					dis.close();
					
					rank=new int[MAX];
					int rank_flag=0;
					len=is_rank.read(buffer);
					while(len!=-1){
						bais=new ByteArrayInputStream(buffer,0,len);
						dis=new DataInputStream(bais);
						for(int v=0;v<(len/4);v++){
							rank[rank_flag++]=dis.readInt();
						}len=is_rank.read(buffer);						
					}
					is_rank.close();
					bais.close();
					dis.close();
					
					graph_map_fun=new int[MAXVERTEX];
					int graphmap_flag=0;
					len=is_graphmap.read(buffer);
					while(len!=-1){
						bais=new ByteArrayInputStream(buffer,0,len);
						dis=new DataInputStream(bais);
						for(int v=0;v<(len/4);v++){
							graph_map_fun[graphmap_flag++]=dis.readInt();
						}len=is_graphmap.read(buffer);						
					}
					is_graphmap.close();
					bais.close();
					dis.close();
					
					buffer=null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
		return is!=null&&LoadIndex(is);		
	}
	
	
	
	public boolean LoadIndex(String filename){
		try{
		BufferedReader br = new BufferedReader(new FileReader(filename+"_conf"));
		MAXVERTEX = Integer.valueOf(br.readLine());
		MAX = Integer.valueOf(br.readLine());
		br.close();
		}catch(Exception e){}
		
		
		return LoadIndex(filename,false);
	}
	
	public boolean StoreIndex(OutputStream os){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		if(index==null) return false;
		try{
			for(int v=0;v<num_v_;v++){
				for(VDPair pis:index.get(v)){
					dos.writeInt(pis.first);
					dos.writeByte(pis.second);
				}dos.writeInt(-1);dos.writeByte(-1);
				os.write(baos.toByteArray());
				baos.reset();				
				os.flush();	
			}	
			os.close();			
		}catch(Exception e){}
		
		
		return true;
		
	}
	public boolean StoreIndex(String filename){
		
		
		
		OutputStream os=null;
		OutputStream os_inv=null;
		OutputStream os_rank=null;
		OutputStream os_graphmap=null;
		try {	
			FileWriter fw = new FileWriter(filename+"_conf");
			fw.write(MAXVERTEX+"\n"+MAX);fw.flush();fw.close();			
			
			os=new FileOutputStream(new File(filename));
			os_inv=new FileOutputStream(new File(filename+"_inv"));
			os_rank=new FileOutputStream(new File(filename+"_rank"));
			os_graphmap= new FileOutputStream(new File(filename+"_graphmap"));
			
			ByteArrayOutputStream baos_inv = new ByteArrayOutputStream();
			DataOutputStream dos_inv = new DataOutputStream(baos_inv);
			for(int i:inv)	dos_inv.writeInt(i);
			os_inv.write(baos_inv.toByteArray());
			os_inv.flush();
			os_inv.close();
			ByteArrayOutputStream baos_rank = new ByteArrayOutputStream();
			DataOutputStream dos_rank = new DataOutputStream(baos_rank);
			for(int i:rank)	dos_rank.writeInt(i);
			os_rank.write(baos_rank.toByteArray());
			os_rank.flush();
			os_rank.close();			
			
			//store the graph_map_fun
			
			ByteArrayOutputStream baos_graphmap = new ByteArrayOutputStream();
			DataOutputStream dos_graphmap = new DataOutputStream(baos_graphmap);
			for(int i:graph_map_fun)	dos_graphmap.writeInt(i);
			os_graphmap.write(baos_graphmap.toByteArray());
			os_graphmap.flush();
			os_graphmap.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return os!=null&&StoreIndex(os);
		}
	
	private void constructfirstMap(String file){
		if(file==null||"".equals(file)) return;
		
		 UndirectedGraph<Integer, DefaultEdge> g =
		            new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		try{			
				InputStream ifs =new FileInputStream(new File(file));
				BufferedReader reader=new BufferedReader(new InputStreamReader(ifs));
				String sr=null;
				
				while((sr=reader.readLine())!=null){
					String[] temp=sr.split(" +");
					if(temp[0].equals(temp[1])) continue;
					if(!g.containsVertex(Integer.valueOf(temp[0]))) g.addVertex(Integer.valueOf(temp[0]));
					if(!g.containsVertex(Integer.valueOf(temp[1]))) g.addVertex(Integer.valueOf(temp[1]));
					g.addEdge(Integer.valueOf(temp[0]), Integer.valueOf(temp[1]));	
				}
				reader.close();
				ifs.close();
			}catch(Exception e){
				e.printStackTrace();
		};
		
		Set<Integer> s=g.vertexSet();		
		int a []=new int[s.size()];
		Iterator<Integer> ii=s.iterator();
		
		int f=0;MAXVERTEX = Integer.MIN_VALUE;	
		while(ii.hasNext()){
			Integer vertex = ii.next();
			MAXVERTEX=MAXVERTEX>vertex?MAXVERTEX:vertex;			
			a[f++]=vertex;
		}
		MAX=s.size();
		MAXVERTEX++;
		graph_map_fun=new int[MAXVERTEX];
		for(int v=0;v<graph_map_fun.length;v++){
			graph_map_fun[v]=-1;
		}
		for(int v=0;v<a.length;v++){
			graph_map_fun[a[v]]=v;
		}
		g=null;
	}
}
