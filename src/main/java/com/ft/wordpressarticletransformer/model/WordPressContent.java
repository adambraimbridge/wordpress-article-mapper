package com.ft.wordpressarticletransformer.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Objects;


public abstract class WordPressContent {
    private final String uuid;
    private final String title;
    private final List<String> titles;
    private final String byline;
    private final SortedSet<Brand> brands;
    private final Date publishedDate;
    private final SortedSet<Identifier> identifiers;
    private final String description;
    private final String mediaType;
    private final Integer pixelWidth;
    private final Integer pixelHeight;
    private final String internalBinaryUrl;
    private final String externalBinaryUrl;
    private final String mainImage;
    private final Comments comments;
    private final String publishReference;

    protected WordPressContent(UUID uuid,
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
                   String publishReference) {
        this.identifiers = identifiers;
        this.comments = comments;
        this.uuid = uuid == null ? null : uuid.toString();
        this.title = title;
        this.titles = titles;
        this.byline = byline;
        this.brands = brands;
        this.publishedDate = publishedDate;
        this.description = description;
        this.mediaType = mediaType;
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.internalBinaryUrl = internalBinaryUrl;
        this.externalBinaryUrl = externalBinaryUrl;
        this.mainImage = mainImage;
        this.publishReference = publishReference;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }
    
    public List<String> getTitles() {
    	return titles;
    }
    
    public String getByline() {
    	return byline;
    }

    public SortedSet<Brand> getBrands() {
		return brands;
	}

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
    public Date getPublishedDate() {
        return publishedDate;
    }

    public SortedSet<Identifier> getIdentifiers() {
        return identifiers;
    }

    public String getDescription() {
        return description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Integer getPixelWidth() {
        return pixelWidth;
    }

    public Integer getPixelHeight() {
        return pixelHeight;
    }

    public String getInternalBinaryUrl() {
        return internalBinaryUrl;
    }

    public String getExternalBinaryUrl() {
        return externalBinaryUrl;
    }

    public Object getMembers() {
        return null;
    }

    public String getMainImage() {
        return mainImage;
    }

    public Comments getComments() {
        return comments;
    }

    public String getPublishReference() {
        return publishReference;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
                .add("uuid", uuid)
                .add("title", title)
                .add("byline", byline)
                .add("brands", brands)
                .add("identifiers", identifiers)
                .add("publishedDate", publishedDate)
                .add("description", description)
                .add("mediaType", mediaType)
                .add("pixelWidth", pixelWidth)
                .add("pixelHeight", pixelHeight)
                .add("internalBinaryUrl", internalBinaryUrl)
                .add("externalBinaryUrl", externalBinaryUrl)
                .add("mainImage", mainImage)
                .add("comments", comments)
                .add("publishReference", publishReference)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordPressContent that = (WordPressContent) o;

        return Objects.equal(this.uuid, that.uuid)
                && Objects.equal(this.title, that.title)
                && Objects.equal(this.byline, that.byline)
                && Objects.equal(this.brands, that.brands)
                && Objects.equal(this.identifiers, that.identifiers)
                && Objects.equal(this.publishedDate, that.publishedDate)
                && Objects.equal(this.description, that.description)
                && Objects.equal(this.mediaType, that.mediaType)
                && Objects.equal(this.pixelWidth, that.pixelWidth)
                && Objects.equal(this.pixelHeight, that.pixelHeight)
                && Objects.equal(this.internalBinaryUrl, that.internalBinaryUrl)
                && Objects.equal(this.externalBinaryUrl, that.externalBinaryUrl)
                && Objects.equal(this.mainImage, that.mainImage)
                && Objects.equal(this.comments, that.comments)
                && Objects.equal(this.publishReference, that.publishReference);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title, byline, brands, identifiers, uuid, publishedDate, description, mediaType, pixelWidth, pixelHeight, internalBinaryUrl, externalBinaryUrl, mainImage, comments, publishReference);
    }

    public abstract static class Builder<C extends WordPressContent> {

        private UUID uuid;
        private String title;
        private List<String> titles;
        private String byline;
        private SortedSet<Brand> brands;
        private Date publishedDate;
        private SortedSet<Identifier> identifiers;
        private String description;
        private String mediaType;
        private Integer pixelWidth;
        private Integer pixelHeight;
        private String internalBinaryUrl;
        private String externalBinaryUrl;
        private String mainImage;
        private Comments comments;
        private String transactionId;

        public Builder<C> withUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public Builder<C> withTitle(String title) {
            this.title = title;
            return this;
        }
        
        public String getTitle() {
            return title;
        }
        
        public Builder<C> withTitles(List<String> titles) {
        	this.titles = titles;
        	if(titles != null) {
        		Collections.sort(titles, new LengthComparator());
        	}
        	return this;
		}
        
        public List<String> getTitles() {
            return titles;
        }
        
        public Builder<C> withByline(String byline) {
            this.byline = byline;
            return this;
        }
        
        public String getByline() {
            return byline;
        }
        
        public Builder<C> withBrands(SortedSet<Brand> brands) {
            this.brands = brands;
            return this;
        }
        
        public SortedSet<Brand> getBrands() {
            return brands;
        }
        
        public Builder<C> withPublishedDate(Date publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }
        
        public Date getPublishedDate() {
            return publishedDate;
        }
        
        public Builder<C> withIdentifiers(SortedSet<Identifier> identifiers) {
            this.identifiers = identifiers;
            return this;
        }
        
        public SortedSet<Identifier> getIdentifiers() {
            return identifiers;
        }
        
        public Builder<C> withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Builder<C> withMediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }
        
        public String getMediaType() {
            return mediaType;
        }
        
        public Builder<C> withPixelWidth(Integer pixelWidth) {
            this.pixelWidth = pixelWidth;
            return this;
        }
        
        public Integer getPixelWidth() {
            return pixelWidth;
        }
        
        public Builder<C> withPixelHeight(Integer pixelHeight) {
            this.pixelHeight = pixelHeight;
            return this;
        }
        
        public Integer getPixelHeight() {
            return pixelHeight;
        }
        
        public Builder<C> withInternalBinaryUrl(String internalDataUrl) {
            this.internalBinaryUrl = internalDataUrl;
            return this;
        }
        
        public String getInternalBinaryUrl() {
            return internalBinaryUrl;
        }
        
        public Builder<C> withExternalBinaryUrl(String externalBinaryUrl) {
            this.externalBinaryUrl = externalBinaryUrl;
            return this;
        }
        
        public String getExternalBinaryUrl() {
            return externalBinaryUrl;
        }
        
        public Builder<C> withMembers(Object members) {
            // no-op
            return this;
        }
        
        public Builder<C> withMainImage(String mainImage) {
            this.mainImage = mainImage;
            return this;
        }
        
        public String getMainImage() {
            return mainImage;
        }
        
        public Builder<C> withComments(Comments comments) {
            this.comments = comments;
            return this;
        }
        
        public Comments getComments() {
            return comments;
        }
        
        public Builder<C> withPublishReference(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public String getPublishReference() {
            return transactionId;
        }
        
        public Builder<C> withValuesFrom(C content) {
            return withTitle(content.getTitle())
            		.withTitles(content.getTitles())
            		.withByline(content.getByline())
            		.withBrands(content.getBrands())
                    .withIdentifiers(content.getIdentifiers())
                    .withUuid(UUID.fromString(content.getUuid()))
                    .withPublishedDate(content.getPublishedDate())
                    .withDescription(content.getDescription())
                    .withMediaType(content.getMediaType())
                    .withPixelWidth(content.getPixelWidth())
                    .withPixelHeight(content.getPixelHeight())
                    .withInternalBinaryUrl(content.getInternalBinaryUrl())
                    .withExternalBinaryUrl(content.getExternalBinaryUrl())
                    .withMainImage(content.getMainImage())
                    .withComments(content.getComments())
                    .withPublishReference(content.getPublishReference());
        }

		public abstract C build();
    }

    private static final class LengthComparator implements Comparator<String>{
		@Override
		public int compare(String o1, String o2) {
			return o1.length() - o2.length();
		}
    }
    
}
