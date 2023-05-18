# Troubleshooting

## UI does not react to changes on Arrays

Due to JavaScript limitations some array Updates may not cause an Update in the components UI. This may happen if you update an item of an array directly, or alter the arrays length. Use the [vm.$set](https://v2.vuejs.org/v2/api/#vm-set) method to update the array in that case, and it will work fine.

```js
//Changing an Value
myArray.$set(index, newValue);

or

//Adding a new Item
this.myArray.push(newArrayItem);
this.$set("myArray", alteredMyArray);
```

Due to dropping IE support with vue 3, this will be a thing of the past, when upgrading.

## Messages of i18n bundle are missing

1. Read [i18n doc](i18n.md)
2. Restart your tomcat
3. Make sure your bundles name is not conflicting with the bundle of another tool or a component's bundle in `webcomponents/bundle/src/main/bundle`
4. Check if your bundle is in a look at the bundles in `tomcat/lib/vuecomponents-bundle-XX-SNAPSHOT.jar`
5. Delete the jar containg the bundles `vuecomponents-bundle-22-SNAPSHOT.jar` and redeploy or deploy on a fresh tomcat

## We want more!

We don't want to run into more problems, but if you do, please add troubleshooting steps to this section or improve the tutorial.