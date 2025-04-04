import { faker } from "@faker-js/faker/locale/en_GB";

export const generateSubmission = () => {

  const firstName = faker.person.firstName();
  const lastName = faker.person.lastName();

  return {
    id: faker.string.uuid(),
    hydrated: true,
    visible: true,
    submitters: [
      {
        id: faker.string.uuid(),
        displayId: faker.number.int(1000),
        sortName: `${lastName}, ${firstName}`,
        displayName: `${firstName} ${lastName}`,
      },
    ],
  };
};
