package com.ft.wordpressarticletransformer.transformer;

import com.ft.wordpressarticletransformer.model.Brand;
import com.ft.wordpressarticletransformer.model.Identifier;
import com.ft.wordpressarticletransformer.model.WordPressLiveBlogContent;
import com.ft.wordpressarticletransformer.resources.BrandSystemResolver;
import com.ft.wordpressarticletransformer.resources.IdentifierBuilder;
import com.ft.wordpressarticletransformer.response.Author;
import com.ft.wordpressarticletransformer.response.Post;

import com.google.common.collect.ImmutableSortedSet;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WordPressLiveBlogContentTransformerTest {
    private static final String TX_ID = "junitTransaction";
    private static final URI REQUEST_URI = URI.create("http://junit.example.org/");
    private static final String POST_URL = "http://junit.example.org/some-post/";
    private static final UUID POST_UUID = UUID.randomUUID();
    private static final OffsetDateTime PUBLISHED_DATE = OffsetDateTime.parse("2015-09-30T15:30:00.000Z");
    private static final String PUBLISHED_DATE_STR = PUBLISHED_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private static final Date LAST_MODIFIED = new Date();
    private static final Set<Brand> BRANDS = new HashSet<Brand>(){{
        add(new Brand("JUNIT-BLOG-BRAND"));
    }};
    private static final String SYSTEM_ID = "http://api.ft.com/system/JUNIT";
    private static final String TITLE = "Test LiveBlog";
    private static final Author AUTHOR = new Author();
    private static final String AUTHOR_NAME = "John Smith";
    private static final String COMMENTS_OPEN = "open";
    private static final SortedSet<Identifier> IDENTIFIERS = ImmutableSortedSet.of(new Identifier(SYSTEM_ID, POST_URL));

    private WordPressLiveBlogContentTransformer transformer;
    private BrandSystemResolver brandResolver = mock(BrandSystemResolver.class);
    private IdentifierBuilder identifierBuilder = mock(IdentifierBuilder.class);

    @Before
    public void setUp() {
        transformer = new WordPressLiveBlogContentTransformer(brandResolver, identifierBuilder);

        when(brandResolver.getBrand(REQUEST_URI)).thenReturn(BRANDS);
        when(identifierBuilder.buildIdentifiers(eq(REQUEST_URI), any(Post.class))).thenReturn(IDENTIFIERS);
        AUTHOR.setName(AUTHOR_NAME);
    }

    @Test
    public void thatLiveBlogPostIsTransformed() {
        Post post = new Post();
        post.setTitle(TITLE);
        post.setDateGmt(PUBLISHED_DATE_STR);
        post.setAuthors(Collections.singletonList(AUTHOR));
        post.setUrl(POST_URL);
        post.setCommentStatus(COMMENTS_OPEN);

        WordPressLiveBlogContent actual = transformer.transform(TX_ID, REQUEST_URI, post, POST_UUID, LAST_MODIFIED);

        assertThat("title", actual.getTitle(), is(equalTo(TITLE)));
        assertThat("byline", actual.getByline(), is(equalTo(AUTHOR_NAME)));
        assertThat("brands", actual.getBrands(), (Matcher) hasItems(BRANDS.toArray()));
        assertThat("identifier authority", actual.getIdentifiers().first().getAuthority(), is(equalTo(SYSTEM_ID)));
        assertThat("identifier value", actual.getIdentifiers().first().getIdentifierValue(), is(equalTo(POST_URL)));
        assertThat("uuid", actual.getUuid(), is(equalTo(POST_UUID.toString())));
        assertThat("realtime", actual.isRealtime(), is(true));
        assertThat("comments", actual.getComments().isEnabled(), is(true));
        assertThat("publishedDate", actual.getPublishedDate().toInstant(), is(equalTo(PUBLISHED_DATE.toInstant())));
        assertThat("lastModified", actual.getLastModified(), is(equalTo(LAST_MODIFIED)));
        assertThat("publishReference", actual.getPublishReference(), is(equalTo(TX_ID)));
    }
}
