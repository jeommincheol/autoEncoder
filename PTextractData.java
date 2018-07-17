import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PTextractData {
	
	static int instr_col = 0;
	static int fragm_col = 13;
	static int modif_col = 6;
	static int CE_col = 16;
	static int charg_col = 12;
	static int pepLen_col = 4;
	static int PIF_col = 29;
	static int andro_col = 24;
	
	static String instr = "NONE";
	static String fragm = "NONE";
	static String modif = "NONE";
	static String CE = "NONE";
	static String charg = "NONE";
	static String pepLen = "NONE";
	static String PIF = "NONE";
	static String andro = "NONE";
	
	static int init = 0;
	
	static BufferedWriter out;
	static int[] lenDis = new int[40];
	static int[][] CEchaDis = new int [3][10];
	
	static int totalPSM = 0;
	
	public static void main(String args[]) throws IOException {
		String dirPath = null;
		String outPath = null;
		
		/*
		 *  All parameter can skip
		 *  -i : input directory Path
		 *  -o : output directory Path
		 *  -mod : modification (Unmodified)
		 *  -CE : Collision energy (1 : 25, 2 : 30, 0 : 35)
		 *  -charge : charge (1,2,3,4,5,6)
		 *  -PIF : minimum PIF (0 ~ 1)
		 *  -andro : andromeda score 
		 *  
		 *    example
		 *    -i E:\AutoEncoder\data\protomeTools\synthetic\HCD\3xHCD -o E:\AutoEncoder\data\protomeTools\synthetic\HCD -frag HCD -mod Unmodified -length 40 -pif 0.8 -andro 100
		 */
		
		for(int j = 0; j < args.length/2; j++) {
			int i = j*2;
			if(args[i].equals("-inst")) {
				instr = args[i+1];
			}
			else if(args[i].equals("-frag")) {
				fragm = args[i+1];
			}
			else if(args[i].equals("-mod")) {
				modif = args[i+1];
			}
			else if(args[i].equals("-ce")) {
				CE = args[i+1];
			}
			else if(args[i].equals("-charge")) {
				charg = args[i+1];
			}
			else if(args[i].equals("-length")) {
				pepLen = args[i+1];
			}
			else if(args[i].equals("-pif")) {
				PIF = args[i+1];
			}
			else if(args[i].equals("-andro")) {
				andro = args[i+1];
			}
			else if(args[i].equals("-i")) {
				dirPath = args[i+1];
			}
			else if(args[i].equals("-o")) {
				outPath = args[i+1];
			}
		}
		
		out = new BufferedWriter(new FileWriter(outPath+"\\orbitrap_"+fragm+"_"+modif+"_"+CE+"_"+charg+"_"+pepLen+"_"+PIF+"_"+andro+".txt"));
		BufferedWriter summary = new BufferedWriter(new FileWriter(outPath+"\\summary_orbitrap_"+fragm+"_"+modif+"_"+CE+"_"+charg+"_"+pepLen+"_"+PIF+"_"+andro+".txt"));
		subDirList(dirPath);
		
		
		summary.write("Total PSM : "+totalPSM);
		summary.newLine();
		summary.newLine();
		
		
		summary.newLine();
		summary.write("Length Distribution");
		summary.newLine();
		for(int i = 0; i < 40; i++) {
			summary.write("Length "+(i+1)+" :\t"+lenDis[i]);
			summary.newLine();
		}
		
		summary.newLine();
		summary.write("charge Distribution");
		summary.newLine();
		for(int i = 0; i < 10; i++) {
			int temp = 0;
			for(int j = 0; j < 3; j++) {
				temp += CEchaDis[j][i];
			}
			summary.write("charge "+(i+1)+" :\t"+temp);
			summary.newLine();
		}
		
		
		summary.newLine();
		summary.write("Collision Energy Distribution");
		summary.newLine();
		for(int i = 0; i < 3; i++) {
			int temp = 0;
			for(int j = 0; j < 10; j++) {
				temp +=CEchaDis[(i+1)%3][j];
			}
			summary.write("CE "+(i+1)+" :\t" +temp);
			summary.newLine();
		}
		
		summary.newLine();
		summary.write("Charge & Collision Energy Distribution");
		summary.newLine();
		
		summary.write("CE\tcharge");
		summary.newLine();
		for(int i = 0; i < 3; i ++) {
			for(int j = 0; j < 10; j++) {
				summary.write("("+(i+1)+","+(j+1)+")"+"\t"+CEchaDis[(i+1)%3][j]);
				summary.newLine();
			}
			summary.newLine();
		}
		
		
		out.close();
		summary.close();
	}
	
	public static void subDirList(String dirPath) throws IOException {
		File dir = new File(dirPath);
		File[]fileList = dir.listFiles();
		
		for(int i = 0; i < fileList.length; i++) {
			File file = fileList[i];
			String fileName = file.getName();
			
			if(file.isDirectory()) {
				System.out.println(file.getCanonicalPath().toString());
				subDirList(file.getCanonicalPath().toString());
			}
			else if(file.getName().equals("msms.txt")) {
				String temp;
				
				BufferedReader br = new BufferedReader(new FileReader(file));
				temp = br.readLine();
				if(init == 0) {
					out.write(temp);
					out.newLine();
				}
				String[] split;
				while(true) {
					temp = br.readLine();
					if(temp == null) {
						break;
					}
					
					split = temp.split("\t");
					if(split.length < 14) {
						System.out.println(temp);
						temp = br.readLine();
						System.out.println(temp);
					}
					if(( instr.equals("NONE") || split[instr_col].equals(instr))&&
							(fragm.equals("NONE") || split[fragm_col].equals(fragm)) &&
							(modif.equals("NONE") || split[modif_col].equals(modif)) &&
							(CE.equals("NONE") || (Integer.parseInt(split[CE_col])%3) == Integer.parseInt(CE)%3) &&
							(charg.equals("NONE") || split[charg_col].equals(charg)) &&
							(pepLen.equals("NONE") || Double.parseDouble(split[pepLen_col]) <=  Double.parseDouble(pepLen)) &&
							(PIF.equals("NONE") || Double.parseDouble(split[PIF_col]) >= Double.parseDouble(PIF)) &&
							(andro.equals("NONE") || Double.parseDouble(split[andro_col]) >= Double.parseDouble(andro))) {
						out.write(temp);
						out.newLine();
						lenDis[Integer.parseInt(split[pepLen_col]) - 1]++;
						CEchaDis[Integer.parseInt(split[CE_col])%3][Integer.parseInt(split[charg_col]) - 1]++;
						totalPSM++;
					}
				}
				br.close();
			}
		}
	}
}
