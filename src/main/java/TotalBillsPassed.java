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

	static class PassedSponsorCount {
		int count = 0;
		Legislator legislator;
		PassedSponsorCount(Legislator legislator) {
			this.legislator = legislator;
		}
	}

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
		mapper.setDateFormat( sdf );

		ZipFile zipFile = new ZipFile( TotalBillsPassed.class.getResource("2013-10-07-ca-json.zip").getFile() );
		TreeMap<String, PassedSponsorCount> passedSponsors = readLegislators(mapper, zipFile);
		TreeMap<String, Committee> committees = readCommittees(mapper, zipFile);
		
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
						PassedSponsorCount sponsorCount = passedSponsors.get(key);
						if ( sponsorCount == null ) {
							key = findCommitteeKey(committees, key);
							if ( key != null ) {
								Committee committee = committees.get(key);
								if ( committee != null ) {
									for ( Committee.Member member: committee.members ) {
										if ( member.role.equals("Chair")) {
											sponsorCount = passedSponsors.get( member.legislatorId );
											cFlag = true;
											break;
										}
									}
								}
							}
						}
						if ( sponsorCount != null && !cFlag ) sponsorCount.count++;
						else if ( !cFlag) System.out.println("Principal Sponsor Not Found:" + bill.sponsors );
					} else {
						System.out.println("Principal Sponsor Not Found:" + bill.sponsors ); 
					}
				}
				continue;
			}
		}
		zipFile.close();
		for ( PassedSponsorCount sponsorCount: passedSponsors.values() ) {
			System.out.println( sponsorCount.legislator.fullName + "\t" + sponsorCount.count );
		}
	}
	
	private static String findCommitteeKey(TreeMap<String, Committee> committees, String namePart) {
		String key = null;
		for ( Committee committee: committees.values() ) {
			if ( committee.committee.contains(namePart)) return committee.id;
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
	
	private static TreeMap<String, PassedSponsorCount> readLegislators(ObjectMapper mapper, ZipFile zipFile) throws Exception {
		TreeMap<String, PassedSponsorCount> legislators = new TreeMap<>();
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while ( entries.hasMoreElements() ) {
			ZipEntry entry = entries.nextElement();
			if ( entry.isDirectory() ) continue;
			String eName = entry.getName();
			if ( eName.contains("legislators")) {
				Legislator legislator = readLegislator(mapper, zipFile, entry);
				if ( legislator.isActive ) legislators.put(legislator.id, new PassedSponsorCount(legislator));
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

}
