import { assertEquals } from "https://deno.land/std@0.115.1/testing/asserts.ts";
import { findPost } from "../../js/conversations/utils.js";

// Simple name and function, compact form, but not configurable
Deno.test("findPost", () => {

  const post1Message = "This is post1";
  const post1_1Message = "This is post1_1";

  const topic = { id: "topic1" };

  let post1 = {
    id: "post1",
    topic: topic.id,
    message: post1Message,
  };

  let post1_1 = {
    id: "post1_1",
    parentPost: post1.id,
    topic: topic.id,
    message: post1_1Message,
  };
  post1.posts = [post1_1];
  topic.posts = [post1];

  post1 = findPost(topic, { postId: post1.id });
  assertEquals(post1.message, post1Message);

  post1_1 = findPost(topic, { postId: post1_1.id });
  assertEquals(post1_1.message, post1_1Message);
});
