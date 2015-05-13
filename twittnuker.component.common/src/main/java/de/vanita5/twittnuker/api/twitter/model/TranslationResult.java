package de.vanita5.twittnuker.api.twitter.model;

public interface TranslationResult extends TwitterResponse {

	public long getId();

	public String getLang();

	public String getText();

	public String getTranslatedLang();

	public String getTranslationType();

}