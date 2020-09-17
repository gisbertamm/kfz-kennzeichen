package kfzkennzeichen.gisbertamm.de.kfz_kennzeichen

import android.app.AlertDialog
import android.app.Fragment
import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.DatabaseHandler
import kfzkennzeichen.gisbertamm.de.kfz_kennzeichen.persistence.SavedEntry
import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.util.*

class ResultFragment : Fragment() {
    val savedEntry: SavedEntry?
        get() = arguments.getSerializable(ENTRY) as SavedEntry?

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        val savedEntry = savedEntry
        val code = view.findViewById<View>(R.id.result_code) as TextView
        val proposeJoke = view.findViewById<View>(R.id.button_propose_joke) as Button
        if (savedEntry == null) { // nothing found
            code.text = getString(R.string.nothing_found)
            proposeJoke.visibility = View.INVISIBLE
            view.findViewById<View>(R.id.result_jokes_heading).visibility = View.INVISIBLE
            return view
        }
        val district = view.findViewById<View>(R.id.result_district) as TextView
        val districtCenter = view.findViewById<View>(R.id.result_district_center) as TextView
        val jokes = view.findViewById<View>(R.id.result_jokes) as TextView
        val crestView = view.findViewById<View>(R.id.crest) as ImageView
        code.text = savedEntry.code
        district.text = savedEntry.district
        district.isClickable = true
        district.setOnClickListener { openWikipediaPage(savedEntry) }
        districtCenter.text = ("abgeleitet von: " + savedEntry.districtCenter
                + " (" + savedEntry._state + ")")
        val jokesText = concatJokesStrings(savedEntry.jokes)
        jokes.text = jokesText
        proposeJoke.setOnClickListener {
            // set an EditText view to get user input; need layout around for padding
            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            layout.gravity = Gravity.CENTER_HORIZONTAL
            val input = EditText(activity)
            val maxLength = 30
            input.filters = arrayOf<InputFilter>(LengthFilter(maxLength))
            layout.setPadding(20, 0, 20, 0)
            layout.addView(input)
            AlertDialog.Builder(activity)
                    .setTitle("Eigenen Text vorschlagen")
                    .setMessage("Bitte Text eingeben (maximal 30 Zeichen).")
                    .setView(layout)
                    .setPositiveButton("Vorschlagen") { dialog, whichButton ->
                        val proposalTask = MailProposalTask()
                        proposalTask.execute(code.text.toString(), input.text.toString())
                    }
                    .setNegativeButton("Abbrechen") { dialog, whichButton ->
                        // Do nothing.
                    }.show()
        }
        val resourceId = resources.getIdentifier(savedEntry.crestIdentifier, "drawable",
                activity.packageName)
        try {
            val d = resources.getDrawable(resourceId)
            if (d != null) {
                crestView.setImageDrawable(d)
            } else {
                Log.e(javaClass.simpleName, "Crest drawable is null for " + savedEntry.code + ", identifier: " + savedEntry.crestIdentifier)
            }
        } catch (e: NotFoundException) {
            Log.e(javaClass.simpleName, "Crest resource not found for " + savedEntry.code + ", identifier: " + savedEntry.crestIdentifier)
        }
        crestView.isClickable = true
        crestView.setOnClickListener { openWikipediaPage(savedEntry) }
        return view
    }

    private fun openWikipediaPage(savedEntry: SavedEntry) {
        val url = DatabaseHandler.WIKIPEDIA_BASE_URL + savedEntry.districtWikipediaUrl
        Log.d(javaClass.simpleName, "Open url: $url")
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun concatJokesStrings(jokesList: List<String>): String {
        val jokesBuff = StringBuilder()
        for (joke in jokesList) {
            jokesBuff.append(joke)
            jokesBuff.append("\n")
        }
        return jokesBuff.toString()
    }

    private inner class MailProposalTask : AsyncTask<String?, Void?, Result>() {
        protected override fun doInBackground(vararg params: String): Result {
            val httpClient: HttpClient = DefaultHttpClient()
            val httpPost = HttpPost("https://api.mailgun.net/v3/sandbox47fa9b0a752440c794641c362d468402.mailgun.org/messages")
            val base64EncodedCredentials = "Basic " + Base64.encodeToString(
                    ("api" + ":" + resources.getString(R.string.api_key)).toByteArray(),
                    Base64.NO_WRAP)
            httpPost.setHeader("Authorization", base64EncodedCredentials)
            val nameValuePairList: MutableList<NameValuePair> = ArrayList()
            val fromBasicNameValuePair = BasicNameValuePair("from", "In-App-Proposal <kfz-kennzeichen-spruch-vorschlag@web.de>")
            nameValuePairList.add(fromBasicNameValuePair)
            val toBasicNameValuePAir = BasicNameValuePair("to", "kfz-kennzeichen-spruch-vorschlag@web.de")
            nameValuePairList.add(toBasicNameValuePAir)
            val subjectBasicNameValuePair = BasicNameValuePair("subject", "Neuer Vorschlag für Kennzeichen-Joke (Android)")
            nameValuePairList.add(subjectBasicNameValuePair)
            val textBasicNameValuePAir = BasicNameValuePair("text", params[0] + "; " + params[1])
            nameValuePairList.add(textBasicNameValuePAir)
            return try {
                val urlEncodedFormEntity = UrlEncodedFormEntity(nameValuePairList, "UTF-8")
                httpPost.entity = urlEncodedFormEntity
                try {
                    val httpResponse = httpClient.execute(httpPost)
                    val result: Result = Result()
                    result.statuscode = httpResponse.statusLine.statusCode
                    result.statusLine = httpResponse.statusLine.reasonPhrase
                    if (httpResponse.statusLine.statusCode != 200) {
                        return result
                    }
                    val inputStream = httpResponse.entity.content
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    val stringBuilder = StringBuilder()
                    var bufferedStrChunk: String? = null
                    while (bufferedReader.readLine().also { bufferedStrChunk = it } != null) {
                        stringBuilder.append(bufferedStrChunk)
                    }
                    result.body = stringBuilder.toString()
                    result
                } catch (cpe: ClientProtocolException) {
                    Log.e(javaClass.simpleName, cpe.localizedMessage, cpe)
                    val result: Result = Result()
                    result.statuscode = -1
                    result.body = cpe.localizedMessage
                    result
                } catch (ioe: IOException) {
                    Log.e(javaClass.simpleName, ioe.localizedMessage, ioe)
                    val result: Result = Result()
                    result.statuscode = -1
                    result.body = ioe.localizedMessage
                    result
                }
            } catch (uee: UnsupportedEncodingException) {
                Log.e(javaClass.simpleName, "Exception given because of UrlEncodedFormEntity argument: code: "
                        + params[0] + ", Vorschlag: " + params[1], uee)
                val result: Result = Result()
                result.statuscode = -1
                result.body = uee.localizedMessage
                result
            }
        }

        override fun onPostExecute(result: Result) {
            super.onPostExecute(result)
            if (result.statuscode == 200) {
                Toast.makeText(activity.applicationContext, "Der Vorschlag wurde erfolgreich übermittelt.", Toast.LENGTH_SHORT).show()
                Log.d(javaClass.simpleName, result.body)
            } else {
                // HTTP error
                var errorMessage: String? = result.statuscode.toString() + "(" + result.statusLine + ")"
                if (result.statuscode == -1) { // Exception
                    errorMessage = result.body
                }
                Toast.makeText(activity.applicationContext, "Der Vorschlag konnte leider nicht übermittelt werden. Fehler: "
                        + errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class Result {
        var statuscode = 0
        var statusLine: String? = null
        var body: String? = null
    }

    companion object {
        const val ENTRY = "entry"
        fun newInstance(entry: SavedEntry?): ResultFragment {
            val f = ResultFragment()
            val args = Bundle()
            args.putSerializable(ENTRY, entry)
            f.arguments = args
            return f
        }
    }
}