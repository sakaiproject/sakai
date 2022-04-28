/*
 *  https://cli.vuejs.org/core-plugins/unit-jest.html
 *  https://vue-test-utils.vuejs.org/
 */

import { shallowMount } from "@vue/test-utils";
import SakaiAvatar from "../../src/components/avatar.vue";

jest.mock(
  "../../../../../../../webcomponents/tool/src/main/frontend/js/sakai-i18n.js",
  () => ({
    loadProperties: () =>
      Promise.resolve({
        avatar_image_alt_of_user: "avatar_image_alt_of_user",
        avatar_image_alt_no_user: "avatar_image_alt_no_user",
      }),
  })
);

const userid = "long-sakai-user-idid";
const username = "Some Users Name";
const siteid = "long-sakai-site-idid";

describe("Avatar: Normal with userid", () => {
  const expectedSrc =
    window.location.protocol +
    "//" +
    window.location.host +
    "/direct/profile/" +
    userid +
    "/image/thumb";
  const wrapper = shallowMount(SakaiAvatar, {
    propsData: {
      userid,
      size: 100,
    },
  });

  it("Shows an image", () => {
    expect(wrapper.find("img").attributes("src")).toEqual(expectedSrc);
    expect(wrapper.find("img").isVisible());
  });

  it("Has image alt without username", () => {
    expect(wrapper.find("img").attributes("alt")).toEqual(
      "avatar_image_alt_no_user"
    );
  });
});

describe("Avatar: Normal with userid + username", () => {
  const wrapper = shallowMount(SakaiAvatar, {
    propsData: {
      userid,
      username,
      size: 100,
    },
  });

  it("Has image alt including username", () => {
    expect(wrapper.find("img").attributes("alt")).toEqual(
      "avatar_image_alt_of_user " + username
    );
  });
});

describe("Avatar: Official with userid", () => {
  const expectedSrc =
    window.location.protocol +
    "//" +
    window.location.host +
    "/direct/profile/" +
    userid +
    "/image/official?siteid=" +
    siteid;
  const wrapper = shallowMount(SakaiAvatar, {
    propsData: {
      userid,
      siteid,
      official: true,
      size: 100,
    },
  });

  it("Shows an image", () => {
    expect(wrapper.find("img").attributes("src")).toEqual(expectedSrc);
    expect(wrapper.find("img").isVisible());
  });
});

describe("Avatar: Size", () => {
  const formats = [
    { name: "Small", nick: "avatar", size: 80 },
    { name: "Medium", nick: "thumb", size: 100 },
    { name: "Large", nick: "default", size: 200 },
  ];

  formats.forEach((format) => {
    let props = {
      userid,
      siteid,
      size: format.size,
    };
    let wrapper = shallowMount(SakaiAvatar, {
      propsData: props,
    });

    it(`${format.name} (${format.size}px) appied correctly to image variant`, () => {
      expect(wrapper.find("img").attributes("height")).toEqual(
        format.size.toString()
      );
      expect(wrapper.find("img").attributes("width")).toEqual(
        format.size.toString()
      );
      expect(wrapper.find("img").attributes("src")).toContain(format.nick);
    });

    props.text = "One two";
    wrapper = shallowMount(SakaiAvatar, {
      propsData: props,
    });

    it(`${format.name} (${format.size}px) appied correctly to text variant`, () => {
      expect(wrapper.find(".avatar > div").attributes("style")).toContain(
        `height: ${format.size}px;`
      );
      expect(wrapper.find(".avatar > div").attributes("style")).toContain(
        `width: ${format.size}px;`
      );
    });
  });

  const wrapper = shallowMount(SakaiAvatar, {
    propsData: {
      userid,
      siteid,
      official: true,
      size: 100,
    },
  });
});

describe("Avatar: Legacyborder", () => {
  it("is appied", () => {
    const wrapper = shallowMount(SakaiAvatar, {
      propsData: {
        userid,
        size: 100,
        legacyborder: true,
      },
    });
    expect(wrapper.element.classList).toContain("avatar-legacy-border");
  });
});
