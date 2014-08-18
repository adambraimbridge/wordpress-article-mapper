@BodyProcessing
Feature: Body processing tag types

  Scenario Outline: Strip element and content
    When I have a StripElementAndContent tag type
    Then the start tag should have been removed
    And the end tag should be removed
    And the content inside should be removed

  Scenario Outline: Strip and leave content
    When I have a Strip tag type
    Then the start tag should have been removed
    And the end tag should be removed
    And the content inside should be present

  Scenario Outline: Retain element, remove attributes
    When I have a RetainElementsWithoutAttributes tag type
    Then the attributes inside the tag should be removed
    And the content inside the tag should be present

  Scenario Outline: Transform one tag into another
    When I have a SimpleTransformTag tag type
    Then the before tag should be replaced with the after tag

  Scenario Outline: Convert HTML entities to unicode
    When I have a PlainTextHTMLEntityReference tag type
    Then the entity should be replaced by the unicode codepoint

