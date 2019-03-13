package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.speech.v1p1beta1.WordInfo;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;

public class AnalyseFile {
	
	/**
	 * Performs non-blocking speech recognition on remote FLAC file and prints
	 * the transcription.
	 *
	 * @param gcsUri
	 *            the path to the remote LINEAR16 audio file to transcribe.
	 */
	
	private List<SpeechRecognitionResult> results;
	private ArrayList<Word> wordParticipant1 = new ArrayList<Word>();
	private ArrayList<Word> wordParticipant2 = new ArrayList<Word>();
	private ArrayList<Sentence> sentenceParticipant1 = new ArrayList<Sentence>();
	private ArrayList<Sentence> sentenceParticipant2 = new ArrayList<Sentence>();
	private ArrayList<Participant> participantData = new ArrayList<Participant>();
	static int numOfParticipants = 1;
	
	public void analyseSoundFile(String gcsUri, int numOfParticipantsKYS) {
		System.out.println("Dette er n�v�rende gcsUri:" + gcsUri);
		//this.numOfParticipants = numOfParticipants;
		
		// Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
		try (SpeechClient speech = SpeechClient.create()) {
			
			// Configure remote file request for Linear16
			RecognitionConfig config = RecognitionConfig.newBuilder()
					.setEncoding(AudioEncoding.LINEAR16)
					.setLanguageCode("NO")
					.setEnableWordTimeOffsets(true)
					.build();
			
			RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();
			
			// Use non-blocking call for getting file transcription
			OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speech
					.longRunningRecognizeAsync(config, audio);
			
			while (!response.isDone()) {
				System.out.println("Venter p� respons...");
				Thread.sleep(10000);
			}

			results = response.get().getResultsList();
			System.out.println("Svar:");
			
			for (SpeechRecognitionResult result : results) {
				// There can be several alternative transcripts for a given
				// chunk of speech. Just use the
				// first (most likely) one here.
				SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
				System.out.printf("Transcription: %s\n", alternative.getTranscript());
				for (WordInfo wordInfo : alternative.getWordsList()) {
					//finner ordet
					String word = wordInfo.getWord();
					
					// finner start tiden
					float startNanosecond = (wordInfo.getStartTime().getNanos() / 100000000);
					float startSecond = wordInfo.getStartTime().getSeconds();
					float timeStampStart = startSecond + (startNanosecond / 10 + (startNanosecond / 100) + (startNanosecond / 1000));
					
					//finner slutt tiden
					float endNanosecond = (wordInfo.getEndTime().getNanos() / 100000000);
					float endSecond = wordInfo.getEndTime().getSeconds();
					float timeStampEnd = endSecond + (endNanosecond / 10) + (endNanosecond / 100) + (endNanosecond / 1000);
					
					if (numOfParticipants == 1)
					{
						wordParticipant1.add(new Word(word, timeStampStart, timeStampEnd));
					}
					if (numOfParticipants == 2)
					{
						wordParticipant2.add(new Word(word, timeStampStart, timeStampEnd));
					}
					
					//System.out.println(wordbank.get(i).getWord() + wordbank.get(i).getStartTime() + wordbank.get(i).getEndTime());
				}
			}
			numOfParticipants++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void constructSentences() {
		//Variabler for � bla gjennom listene
		int i = 0;
		int y = 0;
		//Setningen stringen som sendes brukes i parameter av instansiering for Sentence
		String sentence = null;
		//Midlertidig lagringsplass for startTiden til det f�rste ordet. Brukes for � markere starten av setningen
		List<Float> temporaryStorage = new ArrayList<Float>();
		boolean participant1Complete = false;
		boolean participant2Complete = false;
		
		while (participant1Complete == false && participant2Complete == false) {

				while (wordParticipant1.get(i).getMeanTime() < wordParticipant2.get(y).getMeanTime() && participant1Complete == false) {
					temporaryStorage.add(wordParticipant1.get(i).getStartTime());
					sentence = sentence + " " + wordParticipant1.get(i).getWord();
					if ((i + 1) == wordParticipant1.size()) {
						participant1Complete = true;
						break;
					}
					i++;
				}
				
			if (!temporaryStorage.isEmpty()) {
				sentenceParticipant1.add(new Sentence(sentence, temporaryStorage.get(0), wordParticipant1.get(i).getEndTime()));
				System.out.println("Setning mekka for Person 1:" + sentence);
				temporaryStorage.clear();
				sentence = "";
			}
			
				while (wordParticipant2.get(y).getMeanTime() < wordParticipant1.get(i).getMeanTime() && participant2Complete == false) {
					temporaryStorage.add(wordParticipant2.get(y).getStartTime());
					sentence = sentence + wordParticipant2.get(y).getWord();
					if ((y + 1) == wordParticipant2.size()) {
						participant2Complete = true;
						break;
					}
					y++;
				}
				
			if (!temporaryStorage.isEmpty()) {
			sentenceParticipant2.add(new Sentence(sentence, temporaryStorage.get(0), wordParticipant2.get(y).getEndTime()));
			System.out.println("Setning mekka for Person 2:" + sentence);
			temporaryStorage.clear();
			sentence = "";
			}
			
		}
		System.out.println("Setning metoden funka faktisk.");
		
		
		/*System.out.println("De 10 f�rste ordene i Person1:");
		for (int x = 0; x < wordParticipant1.size(); x++) {
			System.out.println("Ordet til Person 1: " + wordParticipant1.get(x).getWord() + " StartTid: " + wordParticipant1.get(x).getStartTime() + " SluttTid: " + wordParticipant1.get(x).getEndTime());
		}
		System.out.println("De f�rste 10 ordene i Person2");
		for (int x = 0; x < wordParticipant2.size(); x++) {
			System.out.println("Ordet til Person 2: " + wordParticipant2.get(x).getWord() + " StartTid: " + wordParticipant2.get(x).getStartTime() + " SluttTid: " + wordParticipant2.get(x).getEndTime());
		}*/
		
		
		
		/*while (wordParticipant1.size() > i && wordParticipant2.size() > y)													   //Looper s� lenge listene fortsatt har ord i seg
		{
			System.out.println("f�rste while loop starter");
			while (wordParticipant1.get(i).getEndTime() < wordParticipant2.get(y).getStartTime())							   //Sjekk om at ord X ikke overrider med noen ord fra andre personen
			{
				if (wordParticipant1.size() >= i) {
					System.out.println("endtime p� ordet:" + wordParticipant1.get(i).getEndTime());
					System.out.println("starttime p� person 2 ordet:" + wordParticipant2.get(y).getStartTime());
					System.out.println("St�rrelsen p� I:" + i);
					System.out.println("St�rrlesen p� Participant1:" + wordParticipant1.size());
					temporaryStorage.add(wordParticipant1.get(i).getStartTime());												   //Adder startTiden i listen. Bare interessert i nummer 0
					sentence = sentence + wordParticipant1.get(i);                                                                 //Setter ordet i en string
					i++;
				}
			}
			sentenceParticipant1.add(new Sentence(sentence, temporaryStorage.get(0), wordParticipant1.get(i).getEndTime()));   //Instansierer ny sentence objekt inn i listen
			temporaryStorage.clear();																						   //Clearer midlertidig lagringsrommet. Sentence allerede lagd
			sentence = "";																									   //Clearer setningen. Sentence allerede lagd
			
			while (wordParticipant2.get(y).getEndTime() < wordParticipant1.get(i).getStartTime() && i < wordParticipant1.size() && y < wordParticipant2.size())
			{
				System.out.println("hei fra andre while loop lol");
				temporaryStorage.add(wordParticipant2.get(y).getStartTime());
				sentence = sentence + wordParticipant2.get(y);
				y++;
			}
			sentenceParticipant2.add(new Sentence(sentence, temporaryStorage.get(0), wordParticipant1.get(y).getEndTime()));
			temporaryStorage.clear();
			sentence = "";
			System.out.println("St�rrelsen p� I:");
			System.out.println("St�rrelsen p� Y:");
			System.out.println("St�rrlesen p� Participant1:" + wordParticipant1.size());
			System.out.println("St�rrelsen p� Participant2:" + wordParticipant2.size());
		} */
		
		
		//Hver lydfil har sin egen liste. Ord og timestamps blir satt kronologisk inn i listen, s� vi vet at #0 i listen kommer f�r #1, derfor m� vi bare sammenligne liste1.get(0) med liste2.get(0) og
		//se hvem som er f�rst : )
		/*System.out.println(wordParticipant1.size());
		System.out.println("jeg h�per det st�r Dette: " + wordParticipant1.get(0).getWord());
		System.out.println("jeg h�per det st�r en: " + wordParticipant1.get(2).getWord());
		System.out.println("jeg h�per det st�r n� " + wordParticipant1.get(12).getWord()); */
	}
	
	public Participant getParticipantData(int parIndex) {
		return participantData.get(parIndex);
	}
	
}
