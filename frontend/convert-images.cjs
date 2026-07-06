const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const IMAGES = [
  '单词接龙图片(3).png',
  '真人pk图片 (2).png',
  '角色扮演图片 (1).png',
];

const SOURCE_DIRS = [
  path.join(__dirname, 'public', 'photo'),
  path.join(__dirname, '..', 'src', 'main', 'webapp', 'app', 'photo'),
];

async function convert() {
  for (const dir of SOURCE_DIRS) {
    if (!fs.existsSync(dir)) {
      console.log(`⚠ 目录不存在，跳过: ${dir}`);
      continue;
    }
    for (const img of IMAGES) {
      const srcPath = path.join(dir, img);
      if (!fs.existsSync(srcPath)) {
        console.log(`⚠ 文件不存在，跳过: ${srcPath}`);
        continue;
      }
      const stats = fs.statSync(srcPath);
      const sizeMB = (stats.size / 1024 / 1024).toFixed(1);

      const destName = img.replace(/\.png$/i, '.webp');
      const destPath = path.join(dir, destName);

      try {
        await sharp(srcPath)
          .resize({ width: 1200, withoutEnlargement: true })
          .webp({ quality: 80 })
          .toFile(destPath);

        const newStats = fs.statSync(destPath);
        const newSizeMB = (newStats.size / 1024 / 1024).toFixed(1);
        const reduction = ((1 - newStats.size / stats.size) * 100).toFixed(0);
        console.log(`✓ ${img} → ${destName}`);
        console.log(`  ${sizeMB}MB → ${newSizeMB}MB (减小 ${reduction}%)`);
      } catch (err) {
        console.error(`✗ 转换失败 ${img}:`, err.message);
      }
    }
  }
}

convert().catch(console.error);