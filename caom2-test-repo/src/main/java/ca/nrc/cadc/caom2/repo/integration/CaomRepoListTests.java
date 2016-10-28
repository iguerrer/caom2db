/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2011.                            (c) 2011.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 5 $
*
************************************************************************
*/

package ca.nrc.cadc.caom2.repo.integration;

import ca.nrc.cadc.auth.RunnableAction;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;

import javax.security.auth.Subject;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.cadc.caom2.Artifact;
import ca.nrc.cadc.caom2.Chunk;
import ca.nrc.cadc.caom2.Observation;
import ca.nrc.cadc.caom2.Part;
import ca.nrc.cadc.caom2.Plane;
import ca.nrc.cadc.caom2.ProductType;
import ca.nrc.cadc.caom2.ReleaseType;
import ca.nrc.cadc.caom2.SimpleObservation;
import ca.nrc.cadc.caom2.xml.ObservationReader;
import ca.nrc.cadc.date.DateUtil;
import ca.nrc.cadc.net.HttpDownload;
import ca.nrc.cadc.util.Log4jInit;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for caom2repo_ws
 * 
 * @author majorb
 *
 */
public class CaomRepoListTests extends CaomRepoIntTests
{
    
    private static final Logger log = Logger.getLogger(CaomRepoListTests.class);
    
    private DateFormat df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.UTC);
    
    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.caom2", Level.INFO);
    }

    private CaomRepoListTests() { }
    
    /**
     * @param resourceID resource identifier of service to test
     * @param pem1 PEM file for user with read-write permission
     * @param pem2 PEM file for user with read-only permission
     * @param pem3 PEM file for user with no permissions
     */
    public CaomRepoListTests(URI resourceID, String pem1, String pem2, String pem3) 
    { 
        super(resourceID, pem1, pem2, pem3);
    }

    @Test
    public void testListNoReadPermission() throws Throwable
    {
    	int maxRec = 10;    	
    	Date start = new Date(System.currentTimeMillis());

    	// Add a list of observations
    	List<String> baseIDs = new ArrayList<>(Arrays.asList("testListNoReadPermission1", 
    			"testListNoReadPermission2", "testListNoReadPermission3"));
    	List<Observation> observations = this.putObservations(baseIDs);
    	
    	// Check that we have no permission to list the observations
    	checkObservationList(0, super.SCHEME + 
    			super.TEST_COLLECTION, maxRec, start, null, 
    			super.SUBJECT3, null, 403, "permission denied: ", false);
    	
        // cleanup (ok to fail)
    	for (Observation obs : observations)
    	{
	        super.deleteObservation(obs.getURI().toString(), super.SUBJECT1, null, null);
    	}
    }
    
    @Test
    public void testListSuccess() throws Throwable
    {
    	Integer maxRec = 3;    	
    	
    	// Add a list of observations
    	List<String> baseIDs = new ArrayList<>(Arrays.asList("testCleanPutListSuccess1", 
    			"testCleanPutListSuccess2", "testCleanPutListSuccess3"));
    	List<Observation> observations = this.putObservations(baseIDs);
    	Assert.assertTrue("failed to put observations", observations.size() == 3);
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(0).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(1).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(2).getLastModified());
    	Date start = getTime(observations.get(0).getLastModified());
    	Date mid = getTime(observations.get(1).getLastModified());
    	Date end = getTime(observations.get(2).getLastModified());
    	
    	// Check that we have maxRec of the observations
    	checkObservationList(baseIDs.size(), super.SCHEME + TEST_COLLECTION, maxRec, start, 
    			null, super.SUBJECT2, observations, 200, null, true);

    	observations.remove(0);
    	// Check that we only have the last two observations
    	checkObservationList((baseIDs.size() - 1), super.SCHEME + TEST_COLLECTION, maxRec, mid, 
    			null, super.SUBJECT2, observations, 200, null, true);
        
    	observations.remove(0);
    	// Check that we only have the last observation
    	checkObservationList((baseIDs.size() - 2), super.SCHEME + TEST_COLLECTION, maxRec, end, 
    			null, super.SUBJECT2, observations, 200, null, true);
    	
    	// Check that we have maxRec of the observations
    	checkObservationList(1, super.SCHEME + TEST_COLLECTION, maxRec, mid, 
    			end, super.SUBJECT2, observations, 200, null, true);

    	// cleanup (ok to fail)
    	for (Observation obs : observations)
    	{
	        deleteObservation(obs.getURI().toString(), super.SUBJECT1, null, null);
    	}
    }
    
    @Test
    public void testListLessThanMaxRecSuccess() throws Throwable
    {
    	Integer maxRec = 10;    	
    	
    	// Add a list of observations
    	List<String> baseIDs = new ArrayList<>(Arrays.asList("testCleanPutListSuccess1", 
    			"testCleanPutListSuccess2", "testCleanPutListSuccess3"));
    	List<Observation> observations = this.putObservations(baseIDs);
    	Assert.assertTrue("failed to put observations", observations.size() == 3);
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(0).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(1).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(2).getLastModified());
    	Date start = getTime(observations.get(0).getLastModified());
    	Date end = null;
    	
    	// Check that we only have 3 observations
    	checkObservationList(baseIDs.size(), super.SCHEME + TEST_COLLECTION, maxRec, start, 
    			end, super.SUBJECT2, observations, 200, null, true);

    	// cleanup (ok to fail)
    	for (Observation obs : observations)
    	{
	        deleteObservation(obs.getURI().toString(), super.SUBJECT1, null, null);
    	}
    }
    
    @Test
    public void testListMoreThanMaxRecSuccess() throws Throwable
    {
    	Integer maxRec = 2;    	
    	
    	// Add a list of observations
    	List<String> baseIDs = new ArrayList<>(Arrays.asList("testCleanPutListSuccess1", 
    			"testCleanPutListSuccess2", "testCleanPutListSuccess3"));
    	List<Observation> observations = this.putObservations(baseIDs);
    	Assert.assertTrue("failed to put observations", observations.size() == 3);
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(0).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(1).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(2).getLastModified());
    	Date start = getTime(observations.get(0).getLastModified());
    	Date mid = getTime(observations.get(1).getLastModified());
    	Date end = getTime(observations.get(2).getLastModified());
    	
    	// Check that we only have 2 observations
    	checkObservationList(maxRec, super.SCHEME + TEST_COLLECTION, maxRec, start, 
    			null, super.SUBJECT2, observations, 200, null, true);

    	// cleanup (ok to fail)
    	for (Observation obs : observations)
    	{
	        deleteObservation(obs.getURI().toString(), super.SUBJECT1, null, null);
    	}
    }
  
    @Test
    public void testListAllSuccess() throws Throwable
    {
    	Integer maxRec = null;    	
    	
    	// Add a list of observations
    	List<String> baseIDs = new ArrayList<>(Arrays.asList("testCleanPutListSuccess1", 
    			"testCleanPutListSuccess2", "testCleanPutListSuccess3"));
    	List<Observation> observations = this.putObservations(baseIDs);
    	Assert.assertTrue("failed to put observations", observations.size() == 3);
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(0).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(1).getLastModified());
    	Assert.assertNotNull("failed to get first observation maxLastModified date", 
    			observations.get(2).getLastModified());
    	Date start = null;
    	Date end = null;
    	
    	// Check that we have all of the observations
    	checkObservationList(null, super.SCHEME + TEST_COLLECTION, maxRec, start, 
    			end, super.SUBJECT2, observations, 200, null, true);

    	// cleanup (ok to fail)
    	for (Observation obs : observations)
    	{
	        deleteObservation(obs.getURI().toString(), super.SUBJECT1, null, null);
    	}
    }
        
    private List<Observation> putObservations(final List<String> baseIDs) 
    		throws Throwable
    {
    	int i=0;
    	List<Observation> retObs = new ArrayList<Observation>();
    	for (String baseID : baseIDs)
	    {
	        String observationID = generateObservationID(baseID);
	        String uri = super.SCHEME + TEST_COLLECTION + "/" + observationID;
	        	        
	        // create an observation using subject1
	        SimpleObservation observation = new SimpleObservation(
	        		TEST_COLLECTION, observationID);
	        Plane p = new Plane("foo");
	        Artifact a = new Artifact(URI.create("ad:FOO/foo"), 
	        		ProductType.SCIENCE, ReleaseType.DATA);
	        Part pa = new Part(0);
	        Chunk ch = new Chunk();
	        ch.naxis = 0;
	        pa.getChunks().add(ch);
	        a.getParts().add(pa);
	        p.getArtifacts().add(a);
	        observation.getPlanes().add(p);
	        super.putObservation(observation, super.SUBJECT1, 200, "OK", null);
	        
	        // verify the observation using subject2
	        Observation ret = super.getObservation(observation.getURI().toString(), super.SUBJECT2, 200, null);
	        Assert.assertEquals("wrong observation", observation, ret);
	        retObs.add(ret);

	        // separate the puts by 10 ms so that we can pick out each observations easier
	    	TimeUnit.MILLISECONDS.sleep(10);
    	}
    	
    	return retObs;
    }
    
    private Map<String, Date> listObservationIDs(String uri, Integer maxRec, 
    		Date start, Date end, Subject subject, List<Observation> observations, 
    		Integer expectedResponse, String expectedMessage, boolean exactMatch) throws Exception
    {
        log.debug("start list on " + uri);
        
        Map<String, Date> retMap = new Hashtable<String, Date>();
        
        // extract the path from the uri
        URI ouri = new URI(uri);
        String surl = super.BASE_HTTP_URL + "/" + ouri.getSchemeSpecificPart();
        if (subject != null)
        {
            surl = super.BASE_HTTPS_URL + "/" + ouri.getSchemeSpecificPart();
        }
        
        if (maxRec != null)
        	surl = surl + "?maxRec=" + maxRec;
        if (start != null)
            surl = surl + "&start=" + df.format(start);
        if (end != null)
            surl = surl + "&end=" + df.format(end);
        
        URL url = new URL(surl);
        ObservationReader reader = new ObservationReader();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpDownload get = new HttpDownload(url, bos);
        //HttpURLConnection conn = openConnection(subject, urlPath);
        //conn.setRequestMethod("GET");
        
        Subject.doAs(subject, new RunnableAction(get));
        
        int response = get.getResponseCode();
        
        if (expectedResponse != null)
        {
            Assert.assertEquals("wrong response", expectedResponse.intValue(), response);
        }
        
        if (expectedMessage != null)
        {
            String message = bos.toString().trim();
            Assert.assertNotNull(message);
            if (exactMatch)
                Assert.assertEquals("Wrong response message", expectedMessage, message);
            else
                Assert.assertTrue("Wrong response message (startsWith)", message.startsWith(expectedMessage));
        }
        else if (response == 200 && observations != null && observations.size() > 0)
        {
        	long matchSize = observations.size();
        	if (maxRec != null && observations.size() > maxRec)
        	{
        		matchSize = Long.valueOf(maxRec);
        	}
        	
            String message = bos.toString().trim();
            Assert.assertNotNull(message);
            String[] lines = message.split("\\r?\\n");  
	        Assert.assertEquals("wrong number of observation states", matchSize, lines.length);
            for (int i = 0; i < lines.length; i++)
            {            	
            	String[] fields = lines[i].split(",");
            	String actualDate = fields[1];
            	
            	Observation obs = observations.get(i);
            	String expectedDate = df.format(obs.getLastModified());            	
            	Assert.assertEquals("wrong date", expectedDate, actualDate);
            	retMap.put(fields[0], obs.getLastModified());
            }
        }
        
        return retMap;
    }
   
    private void checkObservationList(final Integer expectedSize, 
    		final String collection, final Integer maxRec, final Date start, 
    		final Date end, Subject subject, List<Observation> observations, Integer expectedCode, 
    		String expectedMessage, boolean exactMatch) throws Throwable
    {
    	Map<String, Date> observationIDMap = listObservationIDs(
    			super.SCHEME + TEST_COLLECTION, maxRec, start, end, subject, observations,
    			expectedCode, expectedMessage, exactMatch);  
    	
		// expectedSize == null means no size limits
		if (expectedSize == null)
		{
	        Assert.assertTrue("wrong number of observationIDs", observationIDMap.size() > 0);
		}
		else
		{
	        Assert.assertEquals("wrong number of observationIDs", 
	        		expectedSize.intValue(), observationIDMap.size());
		}

		if (observationIDMap != null && observationIDMap.size() > 0)
    	{
	        
	        Iterator<Date> iter = observationIDMap.values().iterator();
    		while (iter.hasNext())
    		{
		        if (start != null)
		        {
		        	// start date should not be after the date of an observationID
		        	Assert.assertTrue("wrong timestamp", start.compareTo(iter.next()) <= 0);
		        }
		        
		        if (end != null)
		        {
		        	// end date should not be before the date of an observationID
		        	Assert.assertTrue("wrong timestamp", end.compareTo(iter.next()) >= 0);
		        }
    		}
    	}
    }
    
    private Date getTime(Date time)
    {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(time);
    	cal.add(Calendar.MILLISECOND, -5);
    	return cal.getTime();
    }

}
