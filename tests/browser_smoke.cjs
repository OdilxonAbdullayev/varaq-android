const fs=require('fs'),vm=require('vm'),assert=require('assert'),path=require('path');
const elements={};function element(id){return elements[id]||(elements[id]={value:'',innerHTML:'',textContent:'',addEventListener(){},click(){},style:{}})}
const cache={};
const context=vm.createContext({
  document:{getElementById:element,body:{classList:{toggle(){}},appendChild(){}},querySelector(){return null},createElement(){return {style:{},click(){},remove(){},set href(x){},set download(x){}}}},
  localStorage:{getItem:k=>cache[k]||null,setItem:(k,v)=>cache[k]=v},
  window:{},console,setInterval(){return 1},clearInterval(){},setTimeout(){},confirm(){return true},
  Blob:function(){},URL:{createObjectURL(){return 'blob:abc'},revokeObjectURL(){}},
  Intl,Date,Math,Number,String,Array,JSON,Error
});
const script=fs.readFileSync(path.join(__dirname,'..','VARAQ-BROWSER-DEMO.html'),'utf8').split('<script>')[1].split('</script>')[0];vm.runInContext(script,context);
assert(element('app').innerHTML.includes('Tanishib olaylik'));
element('onName').value='Sinov';element('onDaily').value='20';element('onAnnual').value='12';vm.runInContext('onboard()',context);
assert(element('app').innerHTML.includes('Salom, Sinov'));
vm.runInContext('newBook()',context);
assert(element('app').innerHTML.includes('Yangi kitob qo‘shish'));
['bt','ba','bg','bp','bc','bs','br','brev'].forEach((id,i)=>element(id).value=['Sinov kitob','Muallif','Badiiy','100','0','READING','4','Yaxshi'][i]);
vm.runInContext('saveBook()',context);
assert(element('app').innerHTML.includes('Sinov kitob'));
vm.runInContext("navigate('library')",context);assert(element('app').innerHTML.includes('Sinov kitob'));
vm.runInContext('openSession(data.books[0].id)',context);
element('ep').value='20';vm.runInContext('timerSec=1200;finishSession()',context);
vm.runInContext("navigate('stats')",context);assert(element('app').innerHTML.includes('20'));
vm.runInContext("navigate('journal'); newNote()",context);
element('nb').value=String(Date.now()); // actual book id from context
vm.runInContext("document.getElementById('nb').value=String(data.books[0].id)",context);
element('nk').value='THOUGHT';element('nt').value='Kichik qayd';element('np').value='20';vm.runInContext('saveNote()',context);
assert(element('app').innerHTML.includes('Kichik qayd'));
vm.runInContext('openSettings()',context);assert(element('app').innerHTML.includes('Zaxira nusxasi'));
console.log('PASS: onboarding, create book, library, reading session, stats, note, settings');
