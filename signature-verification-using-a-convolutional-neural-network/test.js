const sharp = require('sharp');
const fs = require('fs');

const base = 'test/dutch/';

const genuine = process.argv.slice(2)[0] == 'genuine';
const src = base + (genuine ? 'reference/' : 'questioned/');
const dir = base + (genuine ? 'reference' : 'questioned') + '-gray/';

dirs = fs.readdirSync(src);

try {
	fs.mkdirSync(dir);
} catch (e) {

}

const dic = {};

const start = genuine ? 0 : 4;

for (const clazz of dirs) {

	const files = fs.readdirSync(src + clazz);

	if (!dic[clazz]) {
		dic[clazz] = 0;
	}

	for (const file of files) {

		const count = (++dic[clazz]).toString().padStart(2, '0');

		const out = `${clazz}_${count}.png`

		const fake = file.length > 10;

		sharp(src + clazz + '/' + file)
			.convolve({
				width: 3,
				height: 3,
				kernel: [-1, -1, -1, -1, 8, -1, -1, -1, -1]
			})
			.toColourspace('b-w')
			.resize({ width: 230, height: 150, fit: 'fill' })
			.toFile(dir + `${clazz}_${count}_${fake ? 'f' : 'g'}.png`);

	}

}