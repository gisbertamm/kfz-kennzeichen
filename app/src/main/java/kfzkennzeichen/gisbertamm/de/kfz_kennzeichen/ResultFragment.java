package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.DatabaseHandler;
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry;

public class ResultFragment extends Fragment {

    public static final String ENTRY = "entry";

    public static ResultFragment newInstance(SavedEntry entry) {
        ResultFragment f = new ResultFragment();

        Bundle args = new Bundle();
        args.putSerializable(ENTRY, entry);
        f.setArguments(args);

        return f;
    }

    public SavedEntry getSavedEntry() {
        return (SavedEntry) getArguments().getSerializable(ENTRY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        final TextView code = (TextView) view.findViewById(R.id.result_code);
        TextView district = (TextView) view.findViewById(R.id.result_district);
        TextView districtCenter = (TextView) view.findViewById(R.id.result_district_center);
        TextView jokes = (TextView) view.findViewById(R.id.result_jokes);
        Button proposeJoke = (Button) view.findViewById(R.id.button_propose_joke);

        final SavedEntry savedEntry = getSavedEntry();

        if (savedEntry != null) {
            code.setText(savedEntry.getCode());

            district.setText(savedEntry.getDistrict());
            district.setClickable(true);
            district.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = DatabaseHandler.WIKIPEDIA_BASE_URL + savedEntry.getDistrictWikipediaUrl();
                    Log.d(getClass().getSimpleName(), "Open url: " + url);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
            districtCenter.setText("abgeleitet von: " + savedEntry.getDistrictCenter()
                    + " (" + savedEntry.get_state() + ")");

            String jokesText = concatJokesStrings(savedEntry.getJokes());
            jokes.setText(jokesText);
        } else {
            code.setText(getString(R.string.nothing_found));
        }

        proposeJoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Eigenen Text vorschlagen")
                        .setMessage("Bitte Text eingeben (maximal 30 Zeichen).")
                        .setView(input)
                        .setPositiveButton("Vorschlagen", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                MailProposalTask proposalTask = new MailProposalTask();
                                proposalTask.execute(code.getText().toString(), input.getText().toString());
                            }
                        })
                        .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                        }).show();
            }
        });

        return view;
    }

    private String concatJokesStrings(List<String> jokesList) {
        StringBuilder jokesBuff = new StringBuilder();
        for (String joke : jokesList) {
            jokesBuff.append(joke);
            jokesBuff.append("\n");
        }
        return jokesBuff.toString();
    }

    private class MailProposalTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("https://api.mailgun.net/v3/sandbox47fa9b0a752440c794641c362d468402.mailgun.org/messages");

            String base64EncodedCredentials = "Basic " + Base64.encodeToString(
                    ("api" + ":" + "").getBytes(),
                    Base64.NO_WRAP);


            httpPost.setHeader("Authorization", base64EncodedCredentials);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            BasicNameValuePair fromBasicNameValuePair = new BasicNameValuePair("from", "In-App-Proposal <kfz-kennzeichen-spruch-vorschlag@web.de>");
            nameValuePairList.add(fromBasicNameValuePair);
            BasicNameValuePair toBasicNameValuePAir = new BasicNameValuePair("to", "kfz-kennzeichen-spruch-vorschlag@web.de");
            nameValuePairList.add(toBasicNameValuePAir);
            BasicNameValuePair subjectBasicNameValuePair = new BasicNameValuePair("subject", "Neuer Vorschlag für Kennzeichen-Joke (Android)");
            nameValuePairList.add(subjectBasicNameValuePair);
            BasicNameValuePair textBasicNameValuePAir = new BasicNameValuePair("text", "code: " + params[0] + ", Vorschlag: " + params[1]);
            nameValuePairList.add(textBasicNameValuePAir);

            try {
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);

                httpPost.setEntity(urlEncodedFormEntity);

                try {
                    HttpResponse httpResponse = httpClient.execute(httpPost);

                    InputStream inputStream = httpResponse.getEntity().getContent();

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder stringBuilder = new StringBuilder();

                    String bufferedStrChunk = null;

                    while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
                        stringBuilder.append(bufferedStrChunk);
                    }

                    return stringBuilder.toString();

                } catch (ClientProtocolException cpe) {
                    System.out.println("First Exception caz of HttpResponese :" + cpe);
                    cpe.printStackTrace();
                } catch (IOException ioe) {
                    System.out.println("Second Exception caz of HttpResponse :" + ioe);
                    ioe.printStackTrace();
                }

            } catch (UnsupportedEncodingException uee) {
                System.out.println("An Exception given because of UrlEncodedFormEntity argument :" + uee);
                uee.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}
