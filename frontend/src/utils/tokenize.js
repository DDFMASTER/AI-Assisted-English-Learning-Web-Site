export function tokenizeText(text) {
  const segments = []
  // 匹配英文单词（支持缩写如 don't, it's 和复合词如 well-known）
  const wordRe = /([a-zA-Z]+(?:['-][a-zA-Z]+)*)/g
  let lastIdx = 0
  let match

  while ((match = wordRe.exec(text)) !== null) {
    // 单词之前的纯文本（标点、空格等）
    if (match.index > lastIdx) {
      segments.push({ type: 'text', text: text.slice(lastIdx, match.index) })
    }
    // 单词本身
    segments.push({
      type: 'word',
      text: match[0],
      data: { word: match[0] },
    })
    lastIdx = match.index + match[0].length
  }

  // 末尾剩余文本
  if (lastIdx < text.length) {
    segments.push({ type: 'text', text: text.slice(lastIdx) })
  }

  return segments
}
