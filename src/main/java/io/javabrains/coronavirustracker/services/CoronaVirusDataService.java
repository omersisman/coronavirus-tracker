package io.javabrains.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.javabrains.coronavirustracker.models.LocationStats;

@Service
public class CoronaVirusDataService {
	
	private String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	
	private List<LocationStats> allStats = new ArrayList<>();
	
	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchVirusData() throws IOException, InterruptedException {
		
		List<LocationStats> newStats = new ArrayList<>();
		
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
		
		HttpResponse httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		StringReader csvBodyReader = new StringReader((String) httpResponse.body());
		
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStat = new LocationStats();
			locationStat.setState(record.get("Province/State"));
			locationStat.setCountry(record.get("Country/Region"));
			if(locationStat.getState().isEmpty()) {
				locationStat.setState(record.get("Country/Region"));
			}
			int latestCases = Integer.parseInt(record.get(record.size()-1));
			int prevDayCases = Integer.parseInt(record.get(record.size()-2));
			locationStat.setLatestTotalCases(latestCases);
			locationStat.setDiffFromPrevDay(latestCases-prevDayCases);
		    newStats.add(locationStat);
		}
		this.allStats = newStats;
	}

	public String getVIRUS_DATA_URL() {
		return VIRUS_DATA_URL;
	}

	public void setVIRUS_DATA_URL(String vIRUS_DATA_URL) {
		VIRUS_DATA_URL = vIRUS_DATA_URL;
	}

	public List<LocationStats> getAllStats() {
		return allStats;
	}

	public void setAllStats(List<LocationStats> allStats) {
		this.allStats = allStats;
	}
	
	

}
