import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class mgf2fixedBinning {
	static String dirPath = null;
	static String outPath = null;
	static double minMass = 0;
	static double maxMass = 1000;
	static double binWidth = 1;
	static int count = 0;
	public static void main(String args[]) throws IOException {	
		int i;
		/*
		 * -i : input directory
		 * -o : output directory
		 * -min : min mass
		 * -max : max mass
		 * -bin : bin width
		 */
		for(int j = 0; j < args.length/2; j++) {
			i = j*2;
			
			if(args[i].equals("-i")) {
				dirPath = args[i+1];
			}
			else if(args[i].equals("-o")) {
				outPath = args[i+1];
			}
			else if(args[i].equals("-min")) {
				minMass = Double.parseDouble(args[i+1]);
			}
			else if(args[i].equals("-max")) {
				maxMass = Double.parseDouble(args[i+1]);
			}
			else if(args[i].equals("-bin")) {
				binWidth = Double.parseDouble(args[i+1]);
			}
		}
		makeBin();
		System.out.println("vector size : " + (int) ((maxMass - minMass)/binWidth));
		System.out.println("total count : " + count);
	}
	
	static void makeBin() throws IOException {
		int[] fixBin = new int[(int) ((maxMass - minMass)/binWidth)];
		for(int i = 0; i < fixBin.length; i++) {
			fixBin[i] = 0;
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outPath+"\\"+minMass+"_"+maxMass+"_"+binWidth+".txt"));
		BufferedWriter info = new BufferedWriter(new FileWriter(outPath+"\\"+minMass+"_"+maxMass+"_"+binWidth+".oneSummary"));
		
		File dirFile = new File(dirPath);
		File[] fileList = dirFile.listFiles();
		String in,temp;
		String[] masses;
		
		for(File f : fileList) {
			if(f.isDirectory()) {
				continue;
			}
			in = f.getName();
			BufferedReader br = new BufferedReader(new FileReader(dirPath+"\\"+in));
			
			double binLoc = 0;
			Vector<String> vec = new Vector();
			int fixBinSize = fixBin.length;
			while(true) {
				temp = br.readLine();
				if(temp == null) {
					break;
				}
				if(temp.equals("END IONS")) {
					vec.add(temp);
					masses = new String[vec.size() - 6];
					for(int i = 5; i <vec.size()-1; i++) {
						masses[i-5] = vec.elementAt(i).split(" ")[0];
					}
					
					for(int i = 0; i < masses.length; i++) {
						
						if(Double.parseDouble(masses[i]) > maxMass || Double.parseDouble(masses[i]) < minMass) {
						}
						else {
							binLoc = (Double.parseDouble(masses[i]) - minMass)/binWidth;
							fixBin[(int)binLoc] = 1;
						}
					}
					
					for(int i = 0; i < fixBin.length; i++) {
						bw.write(fixBin[i]+" ");
						fixBin[i] = 0;
					}
					bw.newLine();
					info.write(Integer.toString((vec.size() - 6)));
					info.newLine();

					vec.clear();
					count++;
				}
				else{
					vec.add(temp);
				}
			}
		}
		
		bw.close();
		info.close();
	}
}
