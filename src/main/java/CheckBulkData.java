
import java.util.TimeZone;
import java.util.TreeMap;

import org.openstates.bulkdata.LoadBulkData;
import org.openstates.data.Bill;
import org.openstates.data.Legislator;
import org.openstates.model.Bills;
import org.openstates.model.Legislators;


public class CheckBulkData {

	static class PartyStat {
		int memberCount = 0;
		int billsPassed = 0;
	}

	public static void main(String... args) throws Exception {
		new LoadBulkData().loadCurrentTerm("2013-10-07-ca-json.zip", "20132014", TimeZone.getTimeZone("US/Pacific") );
//		new LoadBulkData().loadCurrentTerm("2013-10-09-mo-json.zip", "2013", TimeZone.getTimeZone("GMT-06:00") );
		// check the Committee.members lists against member lists as defined by the Legislator.roles
		// this will have been woven together in LoadBulkData 
		TreeMap<String, PartyStat> partyStats = new TreeMap<>();
		// setup parties
		determineParties(partyStats);
		// loop on all bills, including resolutions
		for ( Bill bill: Bills.values() ) {
			if ( determinePassed(bill) ) {
				String legislatorId = determinePrincipalAuthor(bill);
				// a committee may be an author
				if ( legislatorId != null ) {
					Legislator legislator = Legislators.get(legislatorId);
					if ( legislator == null ) System.out.println("***: Bill legislatorId " + legislatorId + " references non-existant or inactive legislator for bill:" + bill);
					else if ( legislator.party == null ) System.out.println("***: Legislator Party is null:" + legislator);
					else partyStats.get(legislator.party).billsPassed++; 
				}
			}
		}
		// determine the majority party
		PartyStat majorityParty = determineMajorityParty(partyStats);
		boolean majorityPartyPassedMoreBills = true;
		// show all bills passed
		for ( String party: partyStats.keySet() ) {
			PartyStat partyStat = partyStats.get(party);
			System.out.println("The " + party + " party has " + partyStat.memberCount + " members and passed " + partyStat.billsPassed + " bills.");
			if ( partyStat.billsPassed > majorityParty.billsPassed ) majorityPartyPassedMoreBills = false;
		}
		System.out.println("It is " + majorityPartyPassedMoreBills + " that he majority party passed the most bills.");
	}
	
	private static void determineParties( TreeMap<String, PartyStat> partyStats ) {
		for ( Legislator legislator: Legislators.values() ) {
			PartyStat partyStat = partyStats.get(legislator.party); 
			if ( partyStat == null ) {
				partyStat = new PartyStat();
				partyStats.put(legislator.party, partyStat);
			}
			partyStat.memberCount++;
		}
	}
	
	private static PartyStat determineMajorityParty(TreeMap<String, PartyStat> partyStats) {
		PartyStat majorityParty = null;
		int max = 0;
		for (String party: partyStats.keySet() ) {
			PartyStat partyStat = partyStats.get(party);
			if ( partyStat.billsPassed > max ) {
				majorityParty = partyStat;
				max = partyStat.billsPassed;
			}
		}
		return majorityParty;
	}

	private static String determinePrincipalAuthor(Bill bill) {
		for ( Bill.Sponsor sponsor: bill.sponsors ) {
			if ( sponsor.type.equals("primary") ) {
				return sponsor.leg_id;
			}
		}
		return null;
	}

	private static boolean determinePassed(Bill bill) {
		for ( Bill.Action action: bill.actions ) {
			String act = action.action.toLowerCase();
			if ( act.contains("chaptered") ) return true;
//			if ( act.contains("approved by governor") ) return true;
//			if ( act.contains("signed by governor") ) return true;
		}
		return false;
	}
}
