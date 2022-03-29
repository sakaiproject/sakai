import { cpy } from 'cpy';
import {dependencies} from '../package.json';

function callback(err) {
  if (err) throw err;
  console.error('Error copying files');
}

// destination.txt will be created or overwritten by default.
function copyDependencies() {

    Object.keys(dependencies).forEach(key => {
        await cpy(`node_modules/${key}/**/*`, `./target/assets`);
    });
}

export default copyDependencies;