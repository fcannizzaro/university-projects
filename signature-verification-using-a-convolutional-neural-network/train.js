const sharp = require('sharp');
const fs = require('fs');

const base = 'train/dutch/';

const genuine = process.argv.slice(2)[0] == 'genuine';
const src = base + (genuine ? 'genuine/' : 'forgeries/');
const dir = base + (genuine ? 'genuine' : 'forgeries') + '-gray/';

files = fs.readdirSync(src);

try {
	fs.mkdirSync(dir);
} catch (e) {

}

const dic = {};

const start = genuine ? 0 : 4;

for (const file of files) {

	const iii = file.slice(start, start + 3);

	if (!dic[iii]) {
		dic[iii] = 0;
	}

	const count = (++dic[iii]).toString().padStart(2, '0');

	const out = `${iii}_${count}.png`

	sharp(src + file)
		.convolve({
			width: 3,
			height: 3,
			kernel: [-1, -1, -1, -1, 8, -1, -1, -1, -1]
		})
		.toColourspace('b-w')
		.resize({ width: 230, height: 150, fit: 'fill' })
		.toFile(dir + out);

}