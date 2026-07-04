export const AVATAR_LIST = [
  { id: 1,  file: '1.白猫.svg',   name: '白猫' },
  { id: 2,  file: '2.边牧.svg',   name: '边牧' },
  { id: 3,  file: '3.布偶猫.svg', name: '布偶猫' },
  { id: 4,  file: '4.仓鼠.svg',   name: '仓鼠' },
  { id: 5,  file: '5.藏獒.svg',   name: '藏獒' },
  { id: 6,  file: '6.柴犬.svg',   name: '柴犬' },
  { id: 7,  file: '7.哈士奇.svg', name: '哈士奇' },
  { id: 8,  file: '8.荷兰猪.svg', name: '荷兰猪' },
  { id: 9,  file: '9.黑猫.svg',   name: '黑猫' },
  { id: 10, file: '10.金毛.svg',  name: '金毛' },
  { id: 11, file: '11.橘猫.svg',  name: '橘猫' },
  { id: 12, file: '12.柯基.svg',  name: '柯基' },
  { id: 13, file: '13.可达鸭.svg', name: '可达鸭' },
  { id: 14, file: '14.蓝猫.svg',  name: '蓝猫' },
  { id: 15, file: '15.奶牛猫.svg', name: '奶牛猫' },
  { id: 16, file: '16.三花猫.svg', name: '三花猫' },
  { id: 17, file: '17.田园犬.svg', name: '田园犬' },
  { id: 18, file: '18.暹罗猫.svg', name: '暹罗猫' },
  { id: 19, file: '19.羊.svg',    name: '羊' },
  { id: 20, file: '20.bvvd.png',  name: 'bvvd' },
]

export const AVATAR_BASE_URL = import.meta.env.BASE_URL + 'photo/'

export function getAvatarSrc(avatarId) {
  if (!avatarId || avatarId < 1 || avatarId > AVATAR_LIST.length) return AVATAR_BASE_URL + AVATAR_LIST[0].file
  return AVATAR_BASE_URL + AVATAR_LIST[avatarId - 1].file
}
