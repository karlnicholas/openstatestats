import java.util.TimeZone;
import java.util.TreeMap;

import net.thegreshams.openstates4j.bulkdata.Bills;
import net.thegreshams.openstates4j.bulkdata.Committees;
import net.thegreshams.openstates4j.bulkdata.Legislature;
import net.thegreshams.openstates4j.bulkdata.LoadState;
import net.thegreshams.openstates4j.model.Bill;
import net.thegreshams.openstates4j.model.Committee;
import net.thegreshams.openstates4j.model.Legislator;

public class TotalBillsPassed {

	static class AuthorSuccessStats {
		int billCount = 0;
		int committeeBillCount = 0;
		int officeScore = 0;
		Legislator legislator;
		AuthorSuccessStats(Legislator legislator, int officeScore) {
			this.legislator = legislator;
			this.officeScore = officeScore;
		}
	}

	public static void main(String[] args) throws Exception {

		LoadState.Load( TotalBillsPassed.class.getResource("2013-10-07-ca-json.zip").getFile(), TimeZone.getTimeZone("GMT-08:00") );
		TreeMap<String, AuthorSuccessStats> sponsorSuccess = readLegislators();
		
		for ( Bill bill: Bills.bills() ) {
			boolean passed = determinePassed(bill);
			if ( passed ) {
				String key = determinePrincipalSponsor(bill);
				if ( key != null ) {
					boolean cFlag = false;
					AuthorSuccessStats sponsorStats = sponsorSuccess.get(key);
					if ( sponsorStats == null ) {
						key = Committees.findCommitteeKey(key, bill.chamber);
						if ( key != null ) {
							Committee committee = Committees.get(key);
							if ( committee != null ) {
								for ( Committee.Member member: committee.members ) {
									if ( member.role.toLowerCase().equals("chair")) {
										sponsorStats = sponsorSuccess.get( member.legislatorId );
										cFlag = true;
										break;
									}
								}
							}
						}
					}
					if ( sponsorStats != null && !cFlag ) sponsorStats.billCount++;
					else if ( sponsorStats != null && cFlag ) sponsorStats.committeeBillCount++;
					else if ( !cFlag) System.out.println("Principal Sponsor Not Found:" + bill.sponsors );
				} else {
					System.out.println("Principal Sponsor Not Found:" + bill.sponsors ); 
				}
			}
			continue;
		}
		System.out.println( "NAME" + "\t" + "CHAMBER" + "\t" + "DISTRICT" + "\t" + "PARTY" + "\t" + "OFFICESCORE" + "\t" + "BILLS" + "\t" + "COMMITTEEBILLS"  );
		for ( AuthorSuccessStats sponsorStats: sponsorSuccess.values() ) {
			Legislator legislator = sponsorStats.legislator;
			System.out.println( legislator.fullName + "\t" + legislator.chamber + "\t" + legislator.district + "\t" + legislator.party + "\t" + sponsorStats.officeScore + "\t" + sponsorStats.billCount + "\t" + sponsorStats.committeeBillCount );
		}
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
			if ( action.action.toLowerCase().contains("chaptered") ) return true;
		}
		return passed;
	}
	
	private static TreeMap<String, AuthorSuccessStats> readLegislators() throws Exception {
		TreeMap<String, AuthorSuccessStats> legislators = new TreeMap<>();
		for ( Legislator legislator: Legislature.legislators()) {
			if ( legislator.isActive ) {
				int officeScore = determineOfficeScore(legislator);
				legislators.put(legislator.id, new AuthorSuccessStats(legislator, officeScore));
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
