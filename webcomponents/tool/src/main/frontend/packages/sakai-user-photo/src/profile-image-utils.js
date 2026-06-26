import { getUserId } from "@sakai-ui/sakai-portal-utils";

export const refreshProfileImageTags = (userId, imageType) => {

  const d = new Date();

  const uid = userId?.trim() || "blank";
  const imageUrl = `/api/users/${uid}/profile/image?${d.getTime()}`;

  if (uid === getUserId()) {
    document.querySelectorAll(".sakai-accountProfileImage")
      .forEach(pic => pic.setAttribute("src", imageUrl));
  }

  document.querySelectorAll(`sakai-user-photo[user-id='${CSS.escape(uid)}']`).forEach(up => up.refresh(imageType));
};
