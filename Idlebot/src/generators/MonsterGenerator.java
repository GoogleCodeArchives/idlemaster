package generators;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import data.Monster;
import data.Playable.Alignment;
import events.Battle;

import generators.Utilities.Data;

public class MonsterGenerator {
	
	static Data[] normal;
	static Data[] midboss;
	static Data[] bosses;
	static BossStrings[] strings;
	public static int bossct = 0;
	
	public static class BossStrings {
		
		public String intro, death, kill;
		
		public BossStrings() { this("","",""); }
		
		public BossStrings(String i, String d, String k) {
			this.intro = i;
			this.death = d;
			this.kill = k;
		}
	}
	
	static {
		initialize();
	}

	public static void initialize() {
		try {
			loadMonsters();
			loadStrings();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void loadStrings() {		
		BufferedReader br;
		try {
			br = new BufferedReader(
					new InputStreamReader(
							new URL("http://idlemaster.googlecode.com/svn/trunk/Idlebot/data/monsters/BossStrings.txt").openStream()));

			String strLine;
			
			for(int i=0; i < bosses.length; i++) {
				bossct++;
				BossStrings bs = new BossStrings();
				for(int j=0; j<5; j++) {
					strLine = br.readLine();
					if(strLine == null) continue;
					if(strLine.contains("intro=")){
						bs.intro = strLine.substring(6);
					} else if(strLine.contains("death=")) {
						bs.death = strLine.substring(6);
					} else if(strLine.contains("kills=")) {
						bs.kill = strLine.substring(6);
					}
				}
				strings[i] = bs;
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void loadMonsters() throws MalformedURLException {
			normal = Utilities
			.loadFile(
					new URL(
							"http://idlemaster.googlecode.com/svn/trunk/Idlebot/data/monsters/Monsters.txt"))
			.toArray(GeneratorData.DUMMYARRAY);
			midboss = Utilities
			.loadFile(
					new URL(
							"http://idlemaster.googlecode.com/svn/trunk/Idlebot/data/monsters/MiniBoss.txt"))
			.toArray(GeneratorData.DUMMYARRAY);
			bosses = Utilities
			.loadFile(
					new URL(
							"http://idlemaster.googlecode.com/svn/trunk/Idlebot/data/monsters/Boss.txt"))
			.toArray(GeneratorData.DUMMYARRAY);
			strings = new BossStrings[bosses.length];
	}
	
	public static Monster generateMonster(Alignment alignment, int bosspos) {
		Alignment align = alignment;
		StringBuffer name = new StringBuffer();
		double modPercent = 0;
		int valueBonus = 0;
		BossStrings string = null;
		int tries=0;

		Data monName;
		if(Battle.prob(1) && Battle.prob(50) || bosspos >= 0) {
			int i = bosspos >= 0 ? bosspos : (int) (Math.random() * (bosses.length-1));
			monName = bosses[i];
			string = strings[i];
		}else if(Battle.prob(5)) {
			monName = midboss[(int) (Math.random() * midboss.length)];
		} else {
			monName = normal[(int) (Math.random() * normal.length)];
		}
		
		while(Battle.prob(30) || (monName.name.equals("rarespawn") && tries++ < 10)) {

			Data[] array;
			Data choice;
			
			int i = (int) (Math.random() * 100);
			if (i < 6 && alignment==null) {
				array = GeneratorData.evils;
				align = Alignment.Evil;
			} else if (i < 12 && alignment==null) {
				array = GeneratorData.goods;
				align = Alignment.Good;
			} else if (i < 18 && alignment==null) {
				array = GeneratorData.neutrals;
				align = Alignment.Neutral;
			} else if (i < 33) {
				array = GeneratorData.downvalue;
				modPercent = 0.5;
			} else if (i < 58) {
				array = GeneratorData.upvalue;
				modPercent = 1.5;
			} else {
				array = GeneratorData.prefixes;
			}
			choice = array[(int) (Math.random() * (array.length-1))];
			valueBonus += choice.getValue();
			name.append(choice + " ");
		}
		
		if(Battle.prob(30)) {
			Data color = GeneratorData.getRColor();
			valueBonus += color.getValue();
			name.append(color + " ");
		}
		name.append(monName);
		valueBonus += monName.getValue();
		if(name.toString().equals("rarespawn")) valueBonus*=200;
		
		if(modPercent > 0) valueBonus *= modPercent;
		
		if(align == null) align = Alignment.Good;
		
		if(name.toString().contains("Doppleganger")) name = new StringBuffer("Doppleganger");
		Monster rev = new Monster(name.toString(), align, string);
		rev.addToBonus(valueBonus);
		return rev;
	}
	
}
