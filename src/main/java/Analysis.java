import java.util.*;

import openstatestats.newmodel.*;

import org.openstates.bulkdata.LoadBulkData;
import org.openstates.data.Legislator;
import org.openstates.model.Legislators;


public class Analysis {
	

	public static void main(String... args ) throws Exception {
		Analysis byDistrict = new Analysis();
		byDistrict.buildStateAndTerm();
	}
	
	public Analysis() throws Exception {
		new LoadBulkData().loadCurrentTerm( "2013-10-07-ca-json.zip", "2013", TimeZone.getTimeZone("GMT-08:00") );
	}
	
	public void buildStateAndTerm() {
		StateCapital capitol = new StateCapital(StateCapital.STATES.CA);
		Session session = new Session("2013", capitol.createDate(2013, 1, 1), capitol.createDate(2014, 12, 31));
		
		for ( Legislator legislator: Legislators.values()) {
		}

	}

}
