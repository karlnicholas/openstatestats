import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.thegreshams.openstates4j.model.Bill;
import net.thegreshams.openstates4j.model.Committee;
import net.thegreshams.openstates4j.model.Legislator;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TotalBillsPassed {

	static class SponsorSuccessStats {
		int count = 0;
		int officeScore = 0;
		Legislator legislator;
		SponsorSuccessStats(Legislator legislator, int officeScore) {
			this.legislator = legislator;
			this.officeScore = officeScore;
		}
	}

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
		mapper.setDateFormat( sdf );

		ZipFile zipFile = new ZipFile( TotalBillsPassed.class.getResource("2013-10-07-ca-json.zip").getFile() );
		TreeMap<String, Committee> committees = readCommittees(mapper, zipFile);
		TreeMap<String, SponsorSuccessStats> sponsorSuccess = readLegislators(mapper, zipFile, committees);
		
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while ( entries.hasMoreElements() ) {
			ZipEntry entry = entries.nextElement();
			if ( entry.isDirectory() ) continue;
			String eName = entry.getName();
			if ( eName.contains("ca/20132014")) {
				Bill bill = readBill(mapper, zipFile, entry);
				boolean passed = determinePassed(bill);
				if ( passed ) {
					String key = determinePrincipalSponsor(bill);
					if ( key != null ) {
						boolean cFlag = false;
						SponsorSuccessStats sponsorStats = sponsorSuccess.get(key);
						if ( sponsorStats == null ) {
							key = findCommitteeKey(committees, key, bill.chamber);
							if ( key != null ) {
								Committee committee = committees.get(key);
								if ( committee != null ) {
									for ( Committee.Member member: committee.members ) {
										if ( member.role.equals("Chair")) {
											sponsorStats = sponsorSuccess.get( member.legislatorId );
											cFlag = true;
											break;
										}
									}
								}
							}
						}
						if ( sponsorStats != null && !cFlag ) sponsorStats.count++;
						else if ( !cFlag) System.out.println("Principal Sponsor Not Found:" + bill.sponsors );
					} else {
						System.out.println("Principal Sponsor Not Found:" + bill.sponsors ); 
					}
				}
				continue;
			}
		}
		zipFile.close();
		System.out.println( "NAME" + "\t" + "CHAMBER" + "\t" + "DISTRICT" + "\t" + "PARTY" + "\t" + "OFFICESCORE" + "\t" + "BILLSCHAPTERED"  );
		for ( SponsorSuccessStats sponsorStats: sponsorSuccess.values() ) {
			Legislator legislator = sponsorStats.legislator;
			System.out.println( legislator.fullName + "\t" + legislator.chamber + "\t" + legislator.district + "\t" + legislator.party + "\t" + sponsorStats.officeScore + "\t" + sponsorStats.count  );
		}
	}
	
	private static String findCommitteeKey(TreeMap<String, Committee> committees, String namePart, String chamber) {
		String key = null;
		for ( Committee committee: committees.values() ) {
			if ( namePart.contains(committee.committee) && !namePart.equals(committee.committee) ) System.out.println(namePart);
			if ( (committee.committee.contains(namePart) || namePart.contains(committee.committee)) && (committee.chamber.equals(chamber) || committee.chamber.equals("joint")) ) return committee.id;
		}
		return key;
	}
	
	private static String determinePrincipalSponsor(Bill bill) {
		String key = null;
		for ( Bill.Sponsor sponsor: bill.sponsors ) {
			if ( sponsor.type.equals("primary") ) {
				if ( sponsor.legislatorId != null ) return sponsor.legislatorId;
				else return sponsor.name;
			}
		}
		return key;
	}
	
	private static boolean determinePassed(Bill bill) {
		boolean passed = false;
		for ( Bill.Action action: bill.actions ) {
			if ( action.action.contains("Chaptered") ) return true;
		}
		return passed;
	}
	
	private static TreeMap<String, SponsorSuccessStats> readLegislators(ObjectMapper mapper, ZipFile zipFile, TreeMap<String, Committee> committees) throws Exception {
		TreeMap<String, SponsorSuccessStats> legislators = new TreeMap<>();
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while ( entries.hasMoreElements() ) {
			ZipEntry entry = entries.nextElement();
			if ( entry.isDirectory() ) continue;
			String eName = entry.getName();
			if ( eName.contains("legislators")) {
				Legislator legislator = readLegislator(mapper, zipFile, entry);
				
				if ( legislator.isActive ) {
					int officeScore = determineOfficeScore(legislator, committees);
					legislators.put(legislator.id, new SponsorSuccessStats(legislator, officeScore));
				}
			}
		}
		return legislators;
	}
	
	private static Legislator readLegislator(ObjectMapper mapper, ZipFile zipFile, ZipEntry entry ) throws Exception {
		Legislator legislator = mapper.readValue( zipFile.getInputStream(entry), Legislator.class );
		return legislator;
	}
	
	private static Bill readBill(ObjectMapper mapper, ZipFile zipFile, ZipEntry entry ) throws Exception {
		Bill bill  = mapper.readValue( zipFile.getInputStream(entry), Bill.class );
		return bill;
	}
	
	private static TreeMap<String, Committee> readCommittees(ObjectMapper mapper, ZipFile zipFile) throws Exception {
		TreeMap<String, Committee> committees = new TreeMap<>();
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while ( entries.hasMoreElements() ) {
			ZipEntry entry = entries.nextElement();
			if ( entry.isDirectory() ) continue;
			String eName = entry.getName();
			if ( eName.contains("committees")) {
				Committee committee = readCommittee(mapper, zipFile, entry);
				committees.put(committee.id, committee);
			}
		}
		return committees;
	}
	
	private static Committee readCommittee(ObjectMapper mapper, ZipFile zipFile, ZipEntry entry ) throws Exception {
		Committee committee = mapper.readValue( zipFile.getInputStream(entry), Committee.class );
		return committee;
	}
	
	/**
	 * 
	 * Legislative Influence: Toward Theory Development through Causal Analysis
	 * Author(s): Katherine Meyer
	 * Source: Legislative Studies Quarterly, Vol. 5, No. 4 (Nov., 1980), pp. 563-585
	 * Published
	 * 
	 * It assigned the following values to positions: Party Leader
	 * or Whip = 5; Committee Chair and Vice Chair simultaneously on different
	 * committees = 4; Committee Chair only = 3; two or more Committee Vice
	 * Chairs = 2; Committee Vice Chair only = 1; and Member only = 0.
	 * 
	 * Added -1 if no office held
	 * 
	 */
	private static int determineOfficeScore(Legislator legislator, TreeMap<String, Committee> committees) {
		int score = -1;
		for ( Legislator.Role role: legislator.roles ) {
			String roleType = role.type.toLowerCase();
			if ( role.committee != null ) {
				String key = findCommitteeKey(committees, role.committee, legislator.chamber);
				Committee committee = committees.get( key );
				String nRoleType = findCommitteeRole(committee, legislator);
				if ( nRoleType != null && nRoleType.equals("III") ) System.out.println(committee.committee); 
				if ( nRoleType != null ) roleType = nRoleType.toLowerCase();
			}
			if ( roleType.contains("member")) {
				if ( score == -1 ) score = 0;
			}
			else if ( roleType.equals("vice chair")) {
				if ( score == 0 ) score = 1;
				else if ( score == 1 ) score = 2;
			}
			else if ( roleType.equals("chair") ) {
				if ( score <= 0 ) score = 3;
				else if ( score <= 2 ) score = 4;
			} else { 
				// assume it's a leadership position?
				System.out.println(legislator + ":" + role.type + ":" + roleType);
				score = 5;
			}
		}
		return score;
		
	}
	
	private static String findCommitteeRole( Committee committee, Legislator legislator ) {
		String role = null;
		for ( Committee.Member member: committee.members ) {
			if ( member.legislatorId != null && member.legislatorId.equals( legislator.id) ) return member.role; 
		}
		return role;
	}

}
