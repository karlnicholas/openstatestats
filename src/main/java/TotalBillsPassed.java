import java.util.TimeZone;
import java.util.TreeMap;

import net.thegreshams.openstates4j.bulkdata.Bills;
import net.thegreshams.openstates4j.bulkdata.Committees;
import net.thegreshams.openstates4j.bulkdata.Legislators;
import net.thegreshams.openstates4j.bulkdata.LoadBulkData;
import net.thegreshams.openstates4j.model.Bill;
import net.thegreshams.openstates4j.model.Committee;
import net.thegreshams.openstates4j.model.Legislator;

public class TotalBillsPassed {

	static class AuthorSuccessStats {
		int billCount = 0;
		int committeeBillCount = 0;
		int officeScore;
		AuthorSuccessStats(int officeScore) {
			this.officeScore = officeScore;
		}
	}

	public static void main(String[] args) throws Exception {

		LoadBulkData.LoadCurrentTerm( TotalBillsPassed.class.getResource("2013-10-07-ca-json.zip").getFile(), "20132014", TimeZone.getTimeZone("GMT-08:00") );
		TreeMap<Legislator, AuthorSuccessStats> authorSuccess = readLegislators();
		
		for ( Bill bill: Bills.bills() ) {
			boolean passed = determinePassed(bill);
			if ( passed ) {
				Bill.Sponsor sponsor = determinePrincipalSponsor(bill);
				boolean cFlag = false;
				AuthorSuccessStats authorStats = null;
				Legislator legislator = null;
				if ( sponsor != null && sponsor.legislatorId != null ) {
					legislator = Legislators.get(sponsor.legislatorId);
					if ( legislator != null ) authorStats = authorSuccess.get(legislator);
				}
				// here, legislator.fullName can be really a committee name
				if ( authorStats == null && sponsor != null ) {
					String committeId = null;
					committeId = Committees.findCommitteeKey(sponsor.name, bill.chamber);
					if ( committeId != null ) {
						Committee committee = Committees.get(committeId);
						if ( committee != null ) {
							legislator = determineChair(committee);
							if ( legislator != null ) {
								authorStats = authorSuccess.get( legislator );
								cFlag = true;
							}

						}
					}
				}
				if ( authorStats != null && !cFlag ) authorStats.billCount++;
				else if ( authorStats != null && cFlag ) authorStats.committeeBillCount++;
				else if ( !cFlag) System.out.println("Principal Sponsor Not Found:" + bill.sponsors );
			}
		}
		System.out.println( "NAME" + "\t" + "CHAMBER" + "\t" + "DISTRICT" + "\t" + "PARTY" + "\t" + "OFFICESCORE" + "\t" + "BILLS" + "\t" + "COMMITTEEBILLS"  );
		for ( Legislator legislator: authorSuccess.keySet() ) {
			AuthorSuccessStats authorStats = authorSuccess.get(legislator); 
			System.out.println( legislator.fullName + "\t" + legislator.chamber + "\t" + legislator.district + "\t" + legislator.party + "\t" + authorStats.officeScore + "\t" + authorStats.billCount + "\t" + authorStats.committeeBillCount );
		}
	}

	private static Legislator determineChair(Committee committee ) {
		for ( Committee.Member member: committee.members ) {
			if ( member.role.toLowerCase().equals("chair")) return member.legislator;
		}
		return null;
	}
	private static Bill.Sponsor determinePrincipalSponsor(Bill bill) {
		for ( Bill.Sponsor sponsor: bill.sponsors ) {
			if ( sponsor.type.toLowerCase().equals("primary") ) return sponsor;
		}
		return null;
	}
	
	private static boolean determinePassed(Bill bill) {
		boolean passed = false;
		for ( Bill.Action action: bill.actions ) {
			if ( action.action.toLowerCase().contains("chaptered") ) return true;
		}
		return passed;
	}
	
	private static TreeMap<Legislator, AuthorSuccessStats> readLegislators() throws Exception {
		TreeMap<Legislator, AuthorSuccessStats> legislators = new TreeMap<>();
		for ( Legislator legislator: Legislators.legislators()) {
			if ( legislator.isActive ) {
				int officeScore = determineOfficeScore(legislator);
				legislators.put(legislator, new AuthorSuccessStats(officeScore));
			}
		}
		return legislators;
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
	private static int determineOfficeScore(Legislator legislator) {
		int score = -1;
		for ( Legislator.Role role: legislator.roles ) {
			String roleType = role.type.toLowerCase();
			if ( role.committee != null ) {
				String key = Committees.findCommitteeKey(role.committee.committee, legislator.chamber);
				Committee committee = Committees.get( key );
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
				else if ( score == 1 || score == 2 ) score = 4;
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
			if ( member.legislator.id != null && member.legislator.id.equals( legislator.id) ) return member.role; 
		}
		return role;
	}

}
