import { ref, watch } from 'vue'

const THEME_KEY = 'aael_theme'
const theme = ref(loadTheme())

function loadTheme() {
  return localStorage.getItem(THEME_KEY) || 'system'
}

function saveTheme(val) {
  localStorage.setItem(THEME_KEY, val)
}

function applyTheme(val) {
  const root = document.documentElement
  if (val === 'dark') {
    root.classList.add('dark')
  } else if (val === 'light') {
    root.classList.remove('dark')
  } else {
    // system — follow OS preference
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.classList.toggle('dark', prefersDark)
  }
}

// Apply on load
applyTheme(theme.value)

// Listen for OS theme changes when in 'system' mode
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
  if (theme.value === 'system') {
    document.documentElement.classList.toggle('dark', e.matches)
  }
})

watch(theme, (val) => {
  saveTheme(val)
  applyTheme(val)
})

export function useTheme() {
  function setTheme(val) {
    theme.value = val
  }

  return { theme, setTheme }
}
