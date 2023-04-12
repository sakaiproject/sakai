// This adds a 4-digit string to to the uid field in the components data 
// which can be used for non critical things like input ids 

let calcUid = () => (Math.random() + 1).toString(36).substring(8);

export default {
  created() {
    this.uid = calcUid();
  },
  data() {
    return { uid: null };
  },
};
