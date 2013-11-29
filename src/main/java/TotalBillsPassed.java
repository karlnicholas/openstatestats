import java.util.ArrayList;
import java.util.TimeZone;
import java.util.TreeMap;

import org.openstates.bulkdata.LoadBulkData;
import org.openstates.data.Bill;
import org.openstates.data.Committee;
import org.openstates.data.Legislator;
import org.openstates.model.Bills;
import org.openstates.model.Committees;
import org.openstates.model.Legislators;

public class TotalBillsPassed {

	static class AuthorSuccessStats {
		int billIntroducedCount = 0;
		int billOtherChamberCount = 0;
		int billPassedCount = 0;
		int billChapteredCount = 0;
		int cmember = 0;
		int cvchair = 0;
		int cchair = 0;
		int leader = 0;
		int officeScore = -1;
	}

	public static void main(String[] args) throws Exception {

		new LoadBulkData().loadCurrentTerm( "2013-10-07-ca-json.zip", "2013", TimeZone.getTimeZone("GMT-08:00") );
//		LoadBulkData.LoadCurrentTerm( TotalBillsPassed.class.getResource("2013-10-09-mo-json.zip").getFile(), "2013", TimeZone.getTimeZone("GMT-06:00") );
		TreeMap<Legislator, AuthorSuccessStats> authorSuccess = readLegislators();
		determineOfficeScores(authorSuccess);
		ArrayList<Bill.Sponsor> sponsors = new ArrayList<Bill.Sponsor>();
		for ( Bill bill: Bills.values() ) {
			if (!(bill.bill_id.startsWith("SB") || bill.bill_id.startsWith("AB") || bill.bill_id.startsWith("HB")) ) continue;

			int progress = determineCaProgress(bill);
			sponsors.clear();
			determinePrincipalSponsors(bill, sponsors);
			for ( Bill.Sponsor sponsor: sponsors ) {
				boolean cFlag = false;
				AuthorSuccessStats authorStats = null;
				Legislator legislator = null;
				if ( sponsor != null && sponsor.leg_id != null ) {
					legislator = Legislators.get(sponsor.leg_id);
					if ( legislator != null ) authorStats = authorSuccess.get(legislator);
				}
				if ( authorStats != null && !cFlag ) {
					switch ( progress ) {
					case 0:
						authorStats.billIntroducedCount++;
						break;
					case 1:
						authorStats.billOtherChamberCount++;
						break;
					case 2:
						authorStats.billPassedCount++;
						break;
					case 3:
						authorStats.billChapteredCount++;
						break;
					}
//					if ( bill.id.contains("345") || bill.id.contains("331")) printAllActions(bill);
	
				}
			}
			if ( sponsors.size() == 0 ) System.out.println("Principal Sponsor Not Found:" + bill.sponsors );
		}
		System.out.println( "NAME" + "\t" + "CHAMBER" + "\t" + "DISTRICT" + "\t" + "PARTY" + "\t" + "OFFICESCORE" + "\t" + "BILLSINT" + "\t" + "BILLSOC" + "\t" + "BILLSPASSED" + "\t" + "BILLSCHAP"  );
		for ( Legislator legislator: authorSuccess.keySet() ) {
			AuthorSuccessStats authorStats = authorSuccess.get(legislator); 
			System.out.println( legislator.full_name + "\t" + legislator.chamber + "\t" + legislator.district + "\t" + legislator.party + "\t" + authorStats.officeScore + "\t" + authorStats.billIntroducedCount + "\t" + authorStats.billOtherChamberCount + "\t" + authorStats.billPassedCount + "\t" + authorStats.billChapteredCount );
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
	
	private static int determineMoProgress(Bill bill) {
		int progress = 0;
		for ( Bill.Action action: bill.actions ) {
//			System.out.println(action);
			String act = action.action.toLowerCase();
			if (bill.chamber.equals("lower") && act.contains("reported to the senate") ) progress = 1;
			else if (bill.chamber.equals("upper") && act.contains("reported to the assembly") ) progress = 1;
			else if (act.contains("truly agreed to and finally passed") ) progress = 2;
			else if ( act.contains("approved by governor") ) progress = 3;
			else if ( act.contains("signed by governor") ) progress = 3;
		}
		return progress;
	}
	
	private static int determineCaProgress(Bill bill) {
		int progress = 0;
		for ( Bill.Action action: bill.actions ) {
//			System.out.println(action);
			String act = action.action.toLowerCase();
			if (bill.chamber.equals("lower") && act.contains("to the senate") ) progress = 1;
			else if (bill.chamber.equals("upper") && act.contains("to the assembly") ) progress = 1;
			else if (act.contains("to engrossing and enrolling") ) progress = 2;
			else if ( act.contains("chaptered by secretary of state") ) progress = 3;
			
		}
		return progress;
	}

	private static void printAllActions(Bill bill) {
		for ( Bill.Action action: bill.actions ) {
			System.out.println(action);
		}
	}

	private static TreeMap<Legislator, AuthorSuccessStats> readLegislators() throws Exception {
		TreeMap<Legislator, AuthorSuccessStats> legislators = new TreeMap<>();
		for ( Legislator legislator: Legislators.values()) {
			legislators.put(legislator, new AuthorSuccessStats());
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
	private static void determineOfficeScores(TreeMap<Legislator, AuthorSuccessStats> authorSuccess) {
		for ( Committee committee: Committees.values() ) {
			for ( Committee.Member member: committee.members ) {
				Legislator legislator = null;
				if ( member.leg_id != null ) legislator = Legislators.get(member.leg_id);
				if ( legislator != null ) {
					AuthorSuccessStats successStat = authorSuccess.get(legislator);
					String role = member.role.toLowerCase();
					if ( role.contains("member")) {
						successStat.cmember++;
					}
					else if ( role.equals("vice chair")) {
						successStat.cvchair++;
					}
					else if ( role.equals("chair") ) {
						successStat.cchair++;
					} else { 
						// assume it's a leadership position?
						System.out.println("Leader Role???:" + legislator + ":" + role);
						successStat.leader++;
					}
				}
			}
		}
		// check 
		for (Legislator legislator: authorSuccess.keySet() ) {
			AuthorSuccessStats successStat = authorSuccess.get(legislator); 
			if ( successStat.cmember > 0 ) successStat.officeScore = 0;
			if ( successStat.cvchair == 1 ) successStat.officeScore = 1;
			if ( successStat.cvchair > 1 ) successStat.officeScore = 2;
			if ( successStat.cchair == 1 ) successStat.officeScore = 3;
			if ( successStat.cchair > 0 && successStat.cvchair > 0 ) successStat.officeScore = 4;
			if ( successStat.leader > 0 ) successStat.officeScore = 5;

			for ( Legislator.Role role: legislator.roles ) {
				String type = role.type.toLowerCase();
				if ( !(type.contains("member") || type.contains("vice chair") || type.contains("chair")) ) {
					System.out.println("Presumed leadership?:" + role);
					successStat.officeScore = 5;
				}
			}
			
		}
	}
	
}
