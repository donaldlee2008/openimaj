package org.openimaj.text.nlp;

import gov.sandia.cognition.text.token.DefaultToken;
import gov.sandia.cognition.text.token.Token;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.patterns.AbbreviationPatternProvider;
import org.openimaj.text.nlp.patterns.EmailPatternProvider;
import org.openimaj.text.nlp.patterns.TwitterStuffPatternProvider;
import org.openimaj.text.nlp.patterns.EmbeddedApostrophePatternProvider;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.EntityPatternProvider;
import org.openimaj.text.nlp.patterns.NumberPatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.TimePatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;



public class TweetTokeniser implements Iterable<Token>{
	
	
	private String text;
	private ArrayList<Token> tokenize;
	
	
	public static String regex_or(String ... items )
	{
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}
	public String pos_lookahead(String r){
		return "(?=" + r + ')';
	}
		
	public static String neg_lookahead(String r) {
		return "(?!" + r + ')';
	}
	public String optional(String r){
		return String.format("(%s)?",r);
	}
	
	static EmoticonPatternProvider emoticons = new EmoticonPatternProvider();
	static PunctuationPatternProvider punctuation = new PunctuationPatternProvider();
	static EntityPatternProvider entity = new EntityPatternProvider();
	static URLPatternProvider url = new URLPatternProvider.DFURLPatternProvider();
	static TimePatternProvider time = new TimePatternProvider();
	static NumberPatternProvider number = new NumberPatternProvider();
	static TwitterStuffPatternProvider twitterPart = new TwitterStuffPatternProvider();
	static EmailPatternProvider email = new EmailPatternProvider();
	static AbbreviationPatternProvider abbrev = new AbbreviationPatternProvider(entity);
	private static final String spaceRegex = "\\s+";
	// TODO: FIX EDGE PUNCT
	private static final String EdgePunctArr = new String("[' \" \u201c \u201d \u2018 \u2019 < > \u00AB \u00BB { } \\( \\) \\[ \\]]").replace(" ","");
	private static final String EdgePunct = new String("[' \" \u201c \u201d \u2018 \u2019 < > \u00AB \u00BB { } \\( \\) \\[ \\]]").replace(" ","");
	private static final String NotEdgePunct = "([a-zA-Z0-9]|"+punctuation.charPattern()+"){3}";
	
	private static final String  EdgePunctLeft	= String.format("(\\s|^)(%s+)(%s)",EdgePunct, NotEdgePunct);
	private static final String  EdgePunctRight = String.format("(%s)(%s+)(\\s|$)",NotEdgePunct, EdgePunct);
	private static final Pattern  EdgePunctLeft_RE = Pattern.compile(EdgePunctLeft);
	private static final Pattern EdgePunctRight_RE= Pattern.compile(EdgePunctRight);
	static String Separators = regex_or("--+", "\u2015");
	static String Decorations = new String(" [\u266b]+ ").replace(" ","");
	static EmbeddedApostrophePatternProvider embedded = new EmbeddedApostrophePatternProvider(punctuation);
	
	
	static String [] ProtectThese = new String[]{
			emoticons.patternString(),
			url.patternString(),
			email.patternString(),
			entity.patternString(),
			twitterPart.patternString(),
			time.patternString(),
			number.patternString(),
			punctuation.patternString(),
			abbrev.patternString(),
			Separators,
			Decorations,
			embedded.patternString(),
	};
	static Pattern Protect_RE = Pattern.compile(regex_or(ProtectThese));
	
	public TweetTokeniser(String s) throws UnsupportedEncodingException, TweetTokeniserException{
//		System.out.println(EdgePunct);
//		System.out.println(new String(""));
		this.text = new String(s);
//		System.out.println("TWEET:" + text);
//		fixEncoding();
		squeeze_whitespace();
		simple_tokenize();
		align();
	}
	
	private void align() {
	}

	private void simple_tokenize() throws TweetTokeniserException {
		this.tokenize = new ArrayList<Token>();
		edge_punct_munge();
		
		ArrayList<String> goods = new ArrayList<String>();
		ArrayList<String> bads = new ArrayList<String>();
		ArrayList<Token> res = new ArrayList<Token>();
		int i = 0;
		Matcher matches = Protect_RE.matcher(this.text);
		if(matches!=null)
		{
			while(matches.find()) {
				String goodString = this.text.substring(i,matches.start());
				res.addAll(unprotected_tokenize(goodString));
				String badString = this.text.substring(matches.start(),matches.end());
				bads.add(badString);
				res.add(new DefaultToken(badString,0));
				i = matches.end();
			}
			String finalGood =  this.text.substring(i, this.text.length());
			res.addAll(unprotected_tokenize(finalGood));
		}
		else
		{
			String goodString = this.text.substring(0, this.text.length());
			res.addAll(unprotected_tokenize(goodString));
		}	
		
		this.tokenize = post_process(res);
	}

	private ArrayList<Token> post_process(ArrayList<Token> res) {
		return res;
	}
	private List<Token> unprotected_tokenize(String goodString) {
		String[] strings = goodString.split("\\s+");
		List<Token> t = new ArrayList<Token>();
		for (String s : strings) {
			if(s.isEmpty()) continue;
			t.add(new DefaultToken(s, 0));
		}
		return t;
	}
	private void edge_punct_munge() {
		String s = this.text;
		s = EdgePunctLeft_RE.matcher(s).replaceAll("$1$2 $3");
		s = EdgePunctRight_RE.matcher(s).replaceAll("$1 $2$3");
		this.text = s;
	}

	private void squeeze_whitespace() {
		this.text.replaceAll(spaceRegex, " ");
	}

	private void fixEncoding() throws UnsupportedEncodingException {
		this.text = new String(text.getBytes("UTF-8"),"UTF-8");
		this.text = StringEscapeUtils.unescapeHtml(this.text);
//		System.out.println("UTF-8:" + text);
	}
	@Override
	public Iterator<Token> iterator() {
		return this.tokenize.iterator();
	}
	
	public List<Token> getTokens(){
		return this.tokenize;
	}

}
