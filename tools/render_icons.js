// Rend des icônes OpenMoji (SVG couleur) en PNG 256 × 256 pour l'app.
//
// Ajouter une icône :
//   1. Choisir l'emoji sur https://openmoji.org et noter son code (ex. 1F442 pour l'oreille).
//   2. Télécharger OpenMoji 15.1 (même version que les icônes existantes) :
//        curl -sfo om.tgz https://registry.npmjs.org/openmoji/-/openmoji-15.1.0.tgz && tar xzf om.tgz
//      puis copier package/color/svg/<CODE>.svg vers <dossier svg>/<nom>.svg (ex. ear.svg).
//   3. node tools/render_icons.js <dossier svg> app/src/main/res/drawable-nodpi
//      (Playwright et Chromium sont préinstallés dans l'environnement cloud :
//       NODE_PATH=/opt/node-tools/node_modules si le module n'est pas trouvé).
//   4. Ajouter le nom dans core/.../content/Icons.kt (NAMES), dans app/.../ui/IconImages.kt,
//      et la correspondance nom → code dans docs/licenses/OpenMoji.txt (licence CC BY-SA 4.0).
const { chromium } = require('playwright');
const fs = require('fs');
(async () => {
  const [svgDir, outDir] = process.argv.slice(2);
  const b = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium' });
  const ctx = await b.newContext({ javaScriptEnabled: false, viewport: { width: 256, height: 256 } });
  const p = await ctx.newPage();
  for (const f of fs.readdirSync(svgDir).filter(f => f.endsWith('.svg')).sort()) {
    const svg = fs.readFileSync(svgDir + '/' + f, 'utf8');
    const html = `<html><body style="margin:0;background:transparent"><div style="width:256px;height:256px">${svg.replace(/<svg /, '<svg width="256" height="256" ')}</div></body></html>`;
    await p.setContent(html);
    await p.screenshot({ path: `${outDir}/icon_${f.replace('.svg', '')}.png`, omitBackground: true, clip: { x: 0, y: 0, width: 256, height: 256 } });
  }
  await b.close();
})();
