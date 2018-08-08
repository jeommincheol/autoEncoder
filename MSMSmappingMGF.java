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
	
	static HashMap <String, String> map = new HashMap();
	static BufferedWriter out;
	static BufferedWriter min;
	static BufferedWriter max;
	static BufferedWriter mis;
	static BufferedWriter peakNum;
	static BufferedWriter masses;
	static BufferedWriter intensities;
	
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
		peakNum = new BufferedWriter(new FileWriter(outPath+"\\PeakNum.txt"));
		masses = new BufferedWriter(new FileWriter(outPath+"\\masses.txt"));
		intensities = new BufferedWriter(new FileWriter(outPath+"\\intensities.txt"));
		peakNum.write("#mgfPeak\t#matchedPaek");
		peakNum.newLine();
		System.out.println("Reading MSGF");
		readMGF(mgfPath);
		out.close();
		min.close();
		max.close();
		mis.close();
		peakNum.close();
		masses.close();
		intensities.close();
	}
	
	static void readMGF(String mgfPath) throws IOException {
		File dirFile = new File(mgfPath);
		File[] fileList = dirFile.listFiles();
		String in,temp,title,mass,scan,MSIN;
		String[] ms,inte,io;
		Vector<String> spec = new Vector();
		int mgfCount = 0;
		int msmsCount = 0;
		int matchedNum = 0;
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
						
						MSIN = map.get(in.substring(0,in.lastIndexOf("."))+"\t"+scan);
						io = MSIN.split("\t")[0].split(";");
						ms = MSIN.split("\t")[1].split(";");
						inte = MSIN.split("\t")[2].split(";");
						peakNum.write(Integer.toString(spec.size()-6)+"\t"+MSIN.split("\t")[3]);
						peakNum.newLine();
						
						matchedNum = io.length;
						for(int i = 0; i < matchedNum; i++) {
							if(io[i].startsWith("b") || io[i].startsWith("y")) {
								if(io[i].contains("-")) {
									if(io[i].contains("H2O")) {
										masses.write(ms[i]+" ");
										intensities.write(inte[i]+" ");
									}
								}
								else {
									masses.write(ms[i]+" ");
									intensities.write(inte[i]+" ");
								}
							}
						}
						masses.newLine();
						intensities.newLine();
						
						map.remove(in.substring(0,in.lastIndexOf("."))+"\t"+scan);
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
		System.out.println("-miss matched msms "+ (map.size()));
		Iterator<String> keys = map.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			mis.write(key);
			mis.newLine();
		}
	}
	
	static void readMSMS (String msmsPath) throws IOException {
		File dirFile = new File(msmsPath);
		File[] fileList = dirFile.listFiles();
		String in,temp,mgfName,index,MSIN;
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
					/*
					 * index 37 : matches ion
					 * index 41 : matches masses
					 * index 38 : matches intensity
					 * index 42 : # matched peak
					 */
					MSIN = splitTemp[37]+"\t"+splitTemp[41]+"\t"+splitTemp[38]+"\t"+splitTemp[42];
					map.put(mgfName+"\t"+index, MSIN);
				}
			}
		}
		
		System.out.println("-msms total : "+count);
		System.out.println("-msms duplication "+(count -map.size()));
		System.out.println("-msms unique "+map.size());
		System.out.println();
	}
}
