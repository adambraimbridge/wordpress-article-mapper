package com.ft.wordpressarticlemapper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ft.content.model.AccessLevel;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;


@JsonDeserialize(builder = WordPressLiveBlogContent.Builder.class)
public class WordPressLiveBlogContent
        extends WordPressContent {

    private WordPressLiveBlogContent(UUID uuid,
                                     String title,
                                     List<String> titles,
                                     String byline,
                                     SortedSet<Brand> brands,
                                     SortedSet<Identifier> identifiers,
                                     Date publishedDate,
                                     String description,
                                     String mediaType,
                                     Integer pixelWidth,
                                     Integer pixelHeight,
                                     String internalBinaryUrl,
                                     String externalBinaryUrl,
                                     String mainImage,
                                     Comments comments,
                                     String publishReference,
                                     Date lastModified,
                                     AccessLevel accessLevel) {

        super(uuid, title, titles, byline, brands, identifiers, publishedDate, description, mediaType, pixelWidth,
                pixelHeight, internalBinaryUrl, externalBinaryUrl, mainImage, comments, publishReference, lastModified, accessLevel);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isRealtime() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[realtime=true]", super.toString());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder
            extends WordPressContent.Builder<WordPressLiveBlogContent> {

        public Builder withValuesFrom(WordPressLiveBlogContent content) {
            return ((Builder) super.withValuesFrom(content));
        }

        public WordPressLiveBlogContent build() {
            return new WordPressLiveBlogContent(getUuid(), getTitle(), getTitles(), getByline(),
                    getBrands(), getIdentifiers(), getPublishedDate(), getDescription(),
                    getMediaType(), getPixelWidth(), getPixelHeight(),
                    getInternalBinaryUrl(), getExternalBinaryUrl(),
                    getMainImage(), getComments(), getPublishReference(), getLastModified(), getAccessLevel());
        }
    }
}
