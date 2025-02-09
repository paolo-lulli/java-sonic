package com.github.twohou.sonic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchChannel extends Channel {
    public SearchChannel(String address, Integer port, String password,
                         Integer connectionTimeout, Integer readTimeout)
            throws IOException {
        super(address, port, password, connectionTimeout, readTimeout);
        this.start(Mode.search);
    }

    public ArrayList<String> query(String collection, String bucket, String terms,
                                   Integer limit, Integer offset)
            throws IOException {
        this.send(String.format(
                "%s %s %s \"%s\"%s%s",
                SearchType.QUERY.name(),
                collection,
                bucket,
                terms,
                limit != null ? String.format(" LIMIT(%d)", limit) : "",
                offset != null ? String.format(" OFFSET(%d)", offset) : ""
        ));

        String queryId = assertPendingSearch();
        return assertSearchResults(SearchType.QUERY, queryId);
    }

    public ArrayList<String> query(String collection, String bucket, String terms)
            throws IOException {
        return query(collection, bucket, terms, null, null);
    }

    public ArrayList<String> suggest(String collection, String bucket, String word,
                                     Integer limit)
            throws IOException {
        this.send(String.format(
                "%s %s %s \"%s\"%s",
                SearchType.SUGGEST.name(),
                collection,
                bucket,
                word,
                limit != null ? String.format(" LIMIT(%d)", limit) : ""
        ));

        String searchId = assertPendingSearch();
        return assertSearchResults(SearchType.SUGGEST, searchId);
    }

    public ArrayList<String> suggest(String collection, String bucket, String word)
            throws IOException {
        return suggest(collection, bucket, word, null);
    }

    /**
     * Return pending search ID or throw an exception when the prompt message isn't expected.
     *
     * @return pending search ID
     */
    protected String assertPendingSearch() throws IOException {
        String line1 = this.readLine();
        Matcher matcher1 = Pattern.compile("^PENDING ([a-zA-Z0-9]+)$").matcher(line1);
        if (!matcher1.find()) {
            throw new SonicException("unexpected prompt: " + line1);
        }
        return matcher1.group(1);
    }

    protected ArrayList<String> assertSearchResults(SearchType searchType, String searchId) throws IOException {
        String line2 = this.readLine();
        Matcher matcher2 = Pattern.compile("^EVENT " + searchType.name() + " " + searchId + " (.+)?$")
                .matcher(line2);
        if (!matcher2.find()) {
            throw new SonicException("unexpected prompt: " + line2);
        }

        if (matcher2.groupCount() != 1) {
            return new ArrayList<>();
        } else {
            String[] searchResults = matcher2.group(1).split(" ");
            return new ArrayList<>(Arrays.asList(searchResults));
        }
    }
}
