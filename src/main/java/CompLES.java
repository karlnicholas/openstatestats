import java.io.*;
import java.util.*;

import org.openstates.bulkdata.LoadBulkData;
import org.openstates.data.Bill;
import org.openstates.data.Committee;
import org.openstates.data.Legislator;
import org.openstates.model.Bills;
import org.openstates.model.Committees;
import org.openstates.model.Legislators;

public class CompLES {

	static class AuthorStats {
		public AuthorStats() {
			billData = new int[3][];
			for ( int i=0; i<3; ++i ) {
				billData[i] = new int[4];
				for ( int j=0;j<4;++j) {
					billData[i][j] = 0;
				}
			}
		}
//		int billIntroducedCount = 0;
//		int billOtherChamberCount = 0;
//		int billPassedCount = 0;
//		int billChapteredCount = 0;
		int billData[][];
		int cmember = 0;
		int cvchair = 0;
		int cchair = 0;
		int leader = 0;
		int officeScore = -1;
		double les = 0.0;
	}

	private static TreeSet<String> currentTopics;

	public static void main(String[] args) throws Exception {
		
		TestAction testAction = new CATestAction(); 
		buildcurrentTopics(testAction);
		
		new LoadBulkData().loadCurrentTerm( "2013-10-07-ca-json.zip", "2013", TimeZone.getTimeZone("GMT-08:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-09-mo-json.zip", "2013", TimeZone.getTimeZone("GMT-06:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-08-tx-json.zip", "83", TimeZone.getTimeZone("GMT-06:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-08-ny-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-07-ms-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-07-md-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-08-pa-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-11-01-nj-json.zip", "215", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-08-va-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-07-tn-json.zip", "108", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-07-la-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-11-01-mn-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-09-hi-json.zip", "2013", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-07-az-json.zip", "51st-1st", TimeZone.getTimeZone("GMT-07:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-08-nc-json.zip", "2013", TimeZone.getTimeZone("GMT-07:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-09-ma-json.zip", "187th", TimeZone.getTimeZone("GMT-05:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-09-ok-json.zip", "2013", TimeZone.getTimeZone("GMT-06:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-07-ar-json.zip", "2013", TimeZone.getTimeZone("GMT-06:00") );
//		new LoadBulkData().loadCurrentTerm( "2013-10-07-ga-json.zip", "2013", TimeZone.getTimeZone("GMT-06:00") );
		

		TreeMap<Legislator, AuthorStats> legislatorStats = readLegislators();
		determineOfficeScores(legislatorStats);
		ArrayList<Bill.Sponsor> sponsors = new ArrayList<Bill.Sponsor>();
		Collection<Bill> bills = Bills.values();
		for ( Bill bill:  bills ) {
//			System.out.println(bill.bill_id+"---------------------------------------");
			sponsors.clear();
			determinePrincipalSponsors(bill, sponsors);
			for ( Bill.Sponsor sponsor: sponsors ) {
				Legislator legislator = null;
				AuthorStats sponsorStats = null;
				if ( sponsor != null && sponsor.leg_id != null ) {
					legislator = Legislators.get(sponsor.leg_id);
					if ( legislator != null ) sponsorStats = legislatorStats.get(legislator);
				}
				if ( sponsorStats != null ) determineBillProgress(bill, sponsorStats, testAction);

			}
			if ( sponsors.size() == 0 ) System.out.println("Principal Sponsor Not Found:" + bill.bill_id );
		}
		computeLES(legislatorStats);
		System.out.print( "NAME" + "\t" + "CHAMBER" + "\t" + "DISTRICT" + "\t" + "PARTY" + "\t" + "OFFICE" + "\t");
		System.out.print( "BILLSINT" + "\t" + "BILLSOC" + "\t" + "BILLSPASSED" + "\t" + "BILLSCHAP" + "\t" );
		System.out.print( "BILLSINT" + "\t" + "BILLSOC" + "\t" + "BILLSPASSED" + "\t" + "BILLSCHAP" + "\t" );
		System.out.print( "BILLSINT" + "\t" + "BILLSOC" + "\t" + "BILLSPASSED" + "\t" + "BILLSCHAP" + "\t" );
		System.out.println( "LES");
		for ( Legislator legislator: legislatorStats.keySet() ) {
			AuthorStats sponsorStats = legislatorStats.get(legislator); 
			System.out.print( legislator.full_name + "\t" + legislator.chamber + "\t" + legislator.district + "\t" + legislator.party + "\t" + sponsorStats.officeScore + "\t"  );
			System.out.print( sponsorStats.billData[0][0] + "\t" + sponsorStats.billData[0][1] + "\t" + sponsorStats.billData[0][2] + "\t" + sponsorStats.billData[0][3] + "\t");
			System.out.print( sponsorStats.billData[1][0] + "\t" + sponsorStats.billData[1][1] + "\t" + sponsorStats.billData[1][2] + "\t" + sponsorStats.billData[1][3] + "\t");
			System.out.print( sponsorStats.billData[2][0] + "\t" + sponsorStats.billData[2][1] + "\t" + sponsorStats.billData[2][2] + "\t" + sponsorStats.billData[2][3] + "\t");
			System.out.println( sponsorStats.les );
		}
	}

	/*  code that works for ca, but not sure about anywhere else .. 
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

	 */
	private static void determinePrincipalSponsors(Bill bill, ArrayList<Bill.Sponsor> sponsors) {
		for ( Bill.Sponsor sponsor: bill.sponsors ) {
			if ( sponsor.type.toLowerCase().equals("primary") ) sponsors.add(sponsor);
		}
	}
	
	private static void determineBillProgress(Bill bill, AuthorStats sponsorStats, TestAction testAction) {
		int cat;	// default resolution
		if ( testAction.testId(bill.bill_id) == true ) {
			if ( currentTopics.contains(bill.bill_id) ) {
				System.out.println("Topic: " + bill.bill_id);
				cat = 2;
			}
			else cat = 1;
		}
		else cat = 0;
		
		List<MyAction> actions = new ArrayList<MyAction>();
		for ( Bill.Action action: bill.actions ) {
			actions.add(new MyAction(action));
		}
		Collections.sort(actions);
		
		int progress = 0;
		for ( MyAction myAction: actions ) {
			String act = myAction.action.action.toLowerCase();
//			if ( bill.bill_id.contains("SR") ) System.out.println(bill.bill_id + ":" + bill.chamber+":"+act);
			int tprog = testAction.testAction(bill.chamber, act);
			if ( tprog >= 0 ) progress = tprog;
		}
		sponsorStats.billData[cat][progress]++;

	}

	static class GATestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("senate read and referred") ) return 1;
			else if (chamber.equals("upper") && act.contains("house first readers") ) return 1;
			else if (act.contains("house sent to governor") ) return 2;
			else if (chamber.equals("lower") && act.contains("read and adopted") ) return 3;
			else if (chamber.equals("upper") && act.contains("read and adopted") ) return 3;
			else if ( act.contains("signed by governor") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "GA";
		}
	}

	static class ARTestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("transmitted to the senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("transmitted  to the house") ) return 1;
			else if (chamber.equals("upper") && act.contains("transmitted to the house") ) return 1;
			else if (act.contains("correctly enrolled and ordered transmitted to the governor's office.") ) return 2;
			else if (chamber.equals("lower") && act.contains("read and adopted") ) return 3;
			else if (chamber.equals("upper") && act.contains("read the third time and adopted.") ) return 3;
			else if ( act.contains("is now act ") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "AR";
		}
	}

	static class OKTestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("engrossed, signed, to senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("engrossed to house") ) return 1;
			else if (act.contains("sent to governor") ) return 2;
			else if (chamber.equals("lower") && act.contains("enrolled, signed, filed with secretary of state") ) return 3;
			else if (chamber.equals("upper") && act.contains("enrolled, filed with secretary of state") ) return 3;
			else if ( act.contains("approved by governor") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "OK";
		}
	}

	static class MATestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("H ") || bill_id.contains("S ") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("senate concurred") ) return 1;
			else if (chamber.equals("upper") && act.contains("house concurred") ) return 1;
			else if (act.contains("enacted and laid before the governor") ) return 2;
			else if ( act.contains("signed by the governor") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "MA";
		}
	}

	static class NCTestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("rec from house") ) return 1;
			else if (chamber.equals("upper") && act.contains("rec from senate") ) return 1;
			else if (act.contains("ratified") ) return 2;
			else if (chamber.equals("lower") &&  act.contains("adopted") ) return 3;
			else if (chamber.equals("upper") &&  act.contains("adopted") ) return 3;
			else if ( act.contains("signed by gov.") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "NC";
		}
	}

	static class AZTestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("transmit to house") ) return 1;
			else if (chamber.equals("upper") && act.contains("transmitted to house") ) return 1;
			else if (act.contains("transmitted to governor") ) return 2;
			else if (act.contains("enrolled to governor") ) return 2;
			else if (act.contains("resolution adopted in final form") ) return 3;
			else if (act.contains("transmitted to secretary of state") ) return 3;
			else if ( act.equals("signed") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "AZ";
		}
	}

	static class MNTestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HF") || bill_id.contains("SF") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			return -1;
		}
		@Override
		public String getState() {
			return "MN";
		}
	}

	static class HITestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("transmitted to senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("transmitted to house") ) return 1;
			else if (act.contains("transmitted to governor") ) return 2;
			else if (act.contains("enrolled to governor") ) return 2;
			else if (act.contains("resolution adopted in final form") ) return 3;
			else if (act.contains("certified copies of resolutions sent") ) return 3;
			else if ( act.contains("act ") ) return 3;
			return -1;
		}

		@Override
		public String getState() {
			return "HI";
		}
	}

	static class LATestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB") ) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
//			if ( bill.bill_id.contains("SCR") ) System.out.println(bill.bill_id + ":" + bill.chamber+":"+act);
			if (chamber.equals("lower") && act.contains("received in the senate.") ) return 1;
			else if (chamber.equals("lower") && act.contains("enrolled and signed by the speaker of the house.") ) return 1;
			else if (chamber.equals("upper") && act.contains("received in the house from the senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("ordered sent to the house.") ) return 1;
			else if (act.contains("sent to the governor") ) return 2;
			else if (act.contains("sent to the secretary of state by the secretary") ) return 3;
			else if (act.contains("taken by the clerk of the house and presented to the secretary of state") ) return 3;
			else if ( act.contains("becomes act no.") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "LA";
		}
	}

	static class TNTestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB")) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("ready for transmission to sen") ) return 1;
			else if (chamber.equals("upper") && act.contains("ready for transmission to house") ) return 1;
			else if (act.contains("transmitted to gov. for action") ) return 2;
			else if (act.contains("adopted as am.,  ayes ") ) return 3;
			else if (act.contains("adopted,  ayes ") ) return 3;
			else if ( act.contains("signed by governor") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "TN";
		}
	}


	static class VATestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB")) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("passed house") ) return 1;
			else if (chamber.equals("upper") && act.contains("passed senate") ) return 1;
			else if (act.contains("enrolled") ) return 2;
			else if ( act.contains("enacted, chapter") ) return 3;
			else if (chamber.equals("lower") &&  act.contains("agreed to by house") ) return 3;
			else if (chamber.equals("upper") &&  act.contains("agreed to by senate") ) return 3;
			else if ( act.contains("approved by governor") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "VA";
		}
	}

	static class NJTestAction implements TestAction {
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("A ") || bill_id.contains("S ")) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("received in the senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("received in the assembly") ) return 1;
			else if (act.contains("passed both houses") ) return 2;
			else if ( act.contains("approved p.") ) return 3;
			else if ( act.contains("filed with secretary of state") ) return 3;
			return -1;
		}
		@Override
		public String getState() {
			return "NJ";
		}
		
	}

	static class PATestAction implements TestAction {

		@Override
		public String getState() {
			return "PA";
		}

		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB")) return true;
			return false;
		}

		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("laid on the table") ) return 1;
			else if (chamber.equals("upper") && act.contains("laid on the table") ) return 1;
			else if (act.contains("presented to the governor") ) return 2;
			else if ( act.contains("approved by the governor") ) return 3;
			return -1;
		}
	}

	static class MDTestAction implements TestAction {

		@Override
		public String getState() {
			return "MD";
		}

		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB")) return true;
			return false;
		}

		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("first reading senate rules") ) return 1;
			else if (chamber.equals("upper") && act.contains("first reading") && !act.contains("first reading senate rules")) return 1;
			else if (act.contains("passed enrolled") ) return 2;
			else if (act.contains("returned passed") ) return 2;
			else if ( act.contains("approved by the governor") ) return 3;
			return -1;
		}
		
	}
	static class MSTestAction implements TestAction {

		@Override
		public String getState() {
			return "MS";
		}

		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB")) return true;
			return false;
		}

		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("transmitted to senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("transmitted to house") ) return 1;
			else if (act.contains("enrolled bill signed") ) return 2;
			else if ( act.contains("approved by governor") ) return 3;
			return -1;
		}
		
	}

	static class MOTestAction implements TestAction {
		@Override
		public String getState() {
			return "MO";
		}
		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB")) return true;
			return false;
		}
		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("reported to the senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("reported to the assembly") ) return 1;
			else if (act.contains("truly agreed to and finally passed") ) return 2;
			else if ( act.contains("approved by governor") ) return 3;
			else if ( act.contains("signed by governor") ) return 3;
			return -1;
		}
	}
	static class TXTestAction implements TestAction {

		@Override
		public String getState() {
			return "TX";
		}

		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("HB") || bill_id.contains("SB")) return true;
			return false;
		}

		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("received from the house") ) return 1;
			else if (chamber.equals("upper") && act.contains("received from the senate") ) return 1;
			else if (act.contains("sent to the governor") ) return 2;
			else if ( act.contains("signed by the governor") ) return 3;
			return -1;
		}

	}
	static class NYTestAction implements TestAction {

		@Override
		public String getState() {
			return "NY";
		}

		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("A ") || bill_id.contains("S ")) return true;
			return false;
		}

		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("delivered to senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("delivered to assembly") ) return 1;
			else if (act.contains("delivered to governor") ) return 2;
			else if ( act.contains("signed chap.") ) return 3;
			return -1;
		}
		
	}
	static class CATestAction implements TestAction {

		@Override
		public String getState() {
			return "CA";
		}

		@Override
		public boolean testId(String bill_id) {
			if ( bill_id.contains("SB") || bill_id.contains("AB") || bill_id.contains("SBX1") || bill_id.contains("ABX1")) return true;
			return false;
		}

		@Override
		public int testAction(String chamber, String act) {
			if (chamber.equals("lower") && act.contains("to the senate") ) return 1;
			else if (chamber.equals("lower") && act.contains("in senate") ) return 1;
			else if (chamber.equals("upper") && act.contains("to the assembly") ) return 1;
			else if (chamber.equals("upper") && act.contains("in assembly") ) return 1;
			else if (act.contains("to engrossing and enrolling") ) return 2;
			else if (act.contains("enrolled and presented to the governor") ) return 2;
			else if ( act.contains("approved by the governor") ) return 3;
			else if ( act.contains("chaptered by secretary of state") ) return 3;
			return -1;
		}
		
	}
	private static void printAllActions(Bill bill) {
		for ( Bill.Action action: bill.actions ) {
			System.out.println(action);
		}
	}

	private static TreeMap<Legislator, AuthorStats> readLegislators() throws Exception {
		TreeMap<Legislator, AuthorStats> legislators = new TreeMap<>();
		for ( Legislator legislator: Legislators.values()) {
			legislators.put(legislator, new AuthorStats());
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
	private static void determineOfficeScores(TreeMap<Legislator, AuthorStats> authorSuccess) {
		for ( Committee committee: Committees.values() ) {
			for ( Committee.Member member: committee.members ) {
				Legislator legislator = null;
				if ( member.leg_id != null ) legislator = Legislators.get(member.leg_id);
				if ( legislator != null ) {
					AuthorStats successStat = authorSuccess.get(legislator);
					String role = member.role.toLowerCase();
					if ( role.contains("member")) {
						successStat.cmember++;
					}
					else if ( role.contains("vice")) {
						successStat.cvchair++;
					}
					else if ( role.contains("chair") ) {
						successStat.cchair++;
//					} else { 
						// assume it's a leadership position?
//						System.out.println("Leader Role???:" + legislator + ":" + role);
//						successStat.leader++;
					}
				}
			}
		}
		// check 
		for (Legislator legislator: authorSuccess.keySet() ) {
			AuthorStats successStat = authorSuccess.get(legislator); 
			if ( successStat.cmember > 0 ) successStat.officeScore = 0;
			if ( successStat.cvchair == 1 ) successStat.officeScore = 1;
			if ( successStat.cvchair > 1 ) successStat.officeScore = 2;
			if ( successStat.cchair == 1 ) successStat.officeScore = 3;
			if ( successStat.cchair > 0 && successStat.cvchair > 0 ) successStat.officeScore = 4;
			if ( successStat.leader > 0 ) successStat.officeScore = 5;
/*
			for ( Legislator.Role role: legislator.roles ) {
				String type = role.type.toLowerCase();
				if ( !(type.contains("member") || type.contains("vice chair") || type.contains("chair")) ) {
					System.out.println("Presumed leadership?:" + role);
					successStat.officeScore = 5;
				}
			}
*/			
		}
	}
	
	public static void computeLES(TreeMap<Legislator, AuthorStats> legislators) {
		
//		Map<String, Double> computeLES = (Map<String, Double>)computePad.get(LES); 
		
//		ArrayList<Long> lidsAll = makeRList();
	
		double LESMult = new Double(legislators.size()/4.0);

		double[][] denomArray = new double[3][4];

		denomArray[0][0] = totalFrom(legislators, 0, 0);
		denomArray[0][1] = totalFrom(legislators, 0, 1); 
		denomArray[0][2] = totalFrom(legislators, 0, 2); 
		denomArray[0][3] = totalFrom(legislators, 0, 3); 
		
		denomArray[1][0] = totalFrom(legislators, 1, 0);
		denomArray[1][1] = totalFrom(legislators, 1, 1); 
		denomArray[1][2] = totalFrom(legislators, 1, 2); 
		denomArray[1][3] = totalFrom(legislators, 1, 3); 

		denomArray[2][0] = totalFrom(legislators, 2, 0);
		denomArray[2][1] = totalFrom(legislators, 2, 1); 
		denomArray[2][2] = totalFrom(legislators, 2, 2); 
		denomArray[2][3] = totalFrom(legislators, 2, 3);
		
		// make the array inverse cumulative across rows 
		for ( int j=0; j < 3; ++j ) {
			for ( int i=0; i < 4; ++i ) {
				double sum = 0.0;
				for ( int i2=i; i2 < 4; ++i2 ) {
					sum += denomArray[j][i2]; 
				}
				denomArray[j][i] = sum;
			}
		}

		double billsMult = 5.0;
		double topicMult = 10.0;
		
		
		double[] denom = new double[4];
		denom[0] = denomArray[0][0]
				+ (billsMult * denomArray[1][0])  
				+ (topicMult * denomArray[2][0]); 

		denom[1] = denomArray[0][1]
				+ (billsMult * denomArray[1][1])  
				+ (topicMult * denomArray[2][1]); 

		denom[2] = denomArray[0][2]
				+ (billsMult * denomArray[1][2])  
				+ (topicMult * denomArray[2][2]); 
	
		denom[3] = denomArray[0][3]
				+ (billsMult * denomArray[1][3])  
				+ (topicMult * denomArray[2][3]); 

		double[][] legArray = new double[3][4];

		for ( Legislator key: legislators.keySet()) {
			AuthorStats stats = legislators.get(key);
			
			for ( int i=0; i < 4; ++i ) {
				for ( int j=0; j < 3; ++j ) {
					legArray[j][i] = stats.billData[j][i];
				}
			}
				
			// make the array inverse cumulative across rows 
			for ( int j=0; j < 3; ++j ) {
				for ( int i=0; i < 4; ++i ) {
					double sum = 0.0;
					for ( int i2=i; i2 < 4; ++i2 ) {
						sum += legArray[j][i2]; 
					}
					legArray[j][i] = sum;
				}
			}
	
			double[] num = new double[4];
			num[0] = legArray[0][0]
					+ (billsMult * legArray[1][0])  
					+ (topicMult * legArray[2][0]); 

			num[1] = legArray[0][1]
					+ (billsMult * legArray[1][1])  
					+ (topicMult * legArray[2][1]); 

			num[2] = legArray[0][2]
					+ (billsMult * legArray[1][2])  
					+ (topicMult * legArray[2][2]); 

			num[3] = legArray[0][3]
					+ (billsMult * legArray[1][3])  
					+ (topicMult * legArray[2][3]); 

			double partIntroduced = num[0] / denom[0];			
			double partOtherChamber = num[1] / denom[1];
			double partPassed = num[2] / denom[2];
			double partChaptered = num[3] / denom[3]; 

			double LES = (partIntroduced + partOtherChamber + partPassed + partChaptered) * LESMult;
			stats.les = LES;
		}
	}
	
	private static double totalFrom( TreeMap<Legislator, AuthorStats> legislators, int row, int col) {
		double ret = 0.0;
		for ( Legislator key: legislators.keySet()) {
			AuthorStats stats = legislators.get(key);
			ret = ret + stats.billData[row][col];
//			for ( int i=col; i<4; ++i ) {
//				ret = ret + stats.billData[row][i];
//			}
		}
		return ret;
	}

	private static void buildcurrentTopics(TestAction testAction) throws Exception {
		currentTopics = new TreeSet<String>(); 
		InputStream is = CompLES.class.getResourceAsStream("/" + testAction.getState() + "TopicBills2013.txt");
		InputStreamReader isr = new InputStreamReader(is, "ASCII");
		BufferedReader br = new BufferedReader(isr);
		String line;
		while ( (line = br.readLine()) != null ) {
			currentTopics.add(line);
		}
		is.close();
//		System.out.println(currentTopics);
	}

	interface TestAction {
		public String getState();
		public boolean testId(String bill_id);
		public int testAction(String chamber, String act);
	}
	
	static class MyAction implements Comparable<MyAction> {
		public Bill.Action action; 
		public MyAction(Bill.Action action) {
			this.action = action;
		}
		@Override
		public int compareTo(MyAction o) {
			return action.date.compareTo(o.action.date);
		}
		
	}
}
