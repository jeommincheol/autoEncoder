import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class MSMSmappingMGF {
	
	static HashMap <String, Boolean> map = new HashMap();
	static BufferedWriter out;
	static BufferedWriter min;
	static BufferedWriter max;
	static BufferedWriter mis;
	public static void main(String[] args) throws IOException {
		String msmsPath = null;
		String mgfPath = null;
		String outPath = null;
		
		/*
		 * -ms : msms directory path
		 * -mgf : mgf directory path
		 * -o : output path
		 */
		
		for(int i = 0; i < args.length/2; i++) {
			int index = i*2;
			if(args[index].equals("-ms")) {
				msmsPath = args[index+1];
			}
			else if(args[index].equals("-mgf")) {
				mgfPath = args[index+1];
			}
			else if(args[index].equals("-o")) {
				outPath = args[index+1];
			}
		}
		System.out.println("Reading dataSet");
		readMSMS(msmsPath);
		out = new BufferedWriter(new FileWriter(outPath+"\\msmsMappingMGF.txt"));
		min = new BufferedWriter(new FileWriter(outPath+"\\min.txt"));
		max = new BufferedWriter(new FileWriter(outPath+"\\max.txt"));
		mis = new BufferedWriter(new FileWriter(outPath+"\\missMatch.txt"));
		System.out.println("Reading MSGF");
		readMGF(mgfPath);
		out.close();
		min.close();
		max.close();
		mis.close();
	}
	
	static void readMGF(String mgfPath) throws IOException {
		File dirFile = new File(mgfPath);
		File[] fileList = dirFile.listFiles();
		String in,temp,title,mass,scan;
		Vector<String> spec = new Vector();
		int mgfCount = 0;
		int msmsCount = 0;
		for(File f : fileList) {
			if(f.isDirectory()) {
				continue;
			}
			in = f.getName();
			BufferedReader br = new BufferedReader(new FileReader(mgfPath+"\\"+in));
			
			while(true) {
				temp = br.readLine();
				
				if(temp == null) {
					break;
				}
				
				if(temp.equals("END IONS")) {
					mgfCount++;
					spec.add(temp);
					title = spec.elementAt(1);
					scan = title.substring(title.lastIndexOf("scan=")+5,title.length()-1);
					if(map.containsKey(in.substring(0,in.lastIndexOf("."))+"\t"+scan)) {
						for(int i = 0; i < spec.size(); i++) {
							out.write(spec.elementAt(i));
							out.newLine();
						}
						msmsCount++;
						map.put(in.substring(0,in.lastIndexOf("."))+"\t"+scan, false);
						min.write(spec.elementAt(5));
						min.newLine();
						max.write(spec.elementAt(spec.size()-2));
						max.newLine();
					}
					spec.clear();
				}
				else {
					spec.add(temp);
				}
			}
		}
		System.out.println("-total mgf "+mgfCount);
		System.out.println("-matched msms "+msmsCount);
		System.out.println("-miss matched msms "+ (map.size() - msmsCount));
		Iterator<String> keys = map.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			if(map.get(key)) {
				mis.write(key);
				mis.newLine();
			}
		}
	}
	
	static void readMSMS (String msmsPath) throws IOException {
		File dirFile = new File(msmsPath);
		File[] fileList = dirFile.listFiles();
		String in,temp,mgfName,index;
		String[] splitTemp;
		HashMap<String,Boolean> tempMap = new HashMap();
		int count = 0;
		
		for(File f : fileList) {
			if(f.isDirectory()) {
				continue;
			}
			in = f.getName();
			BufferedReader br = new BufferedReader(new FileReader(msmsPath+"\\"+in));
			temp = br.readLine();
			while(true) {
				temp = br.readLine();
				if(temp == null) {
					break;
				}
				count++;
				splitTemp = temp.split("\t");
				mgfName = splitTemp[0];
				index = splitTemp[1];
				if(!map.containsKey(mgfName+"\t"+index)) {
					map.put(mgfName+"\t"+index, true);
				}
			}
		}
		
		System.out.println("-msms total : "+count);
		System.out.println("-msms duplication "+(count -map.size()));
		System.out.println("-msms unique "+map.size());
		System.out.println();
	}
}
