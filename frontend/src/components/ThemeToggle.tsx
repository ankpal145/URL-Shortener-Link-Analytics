import { Moon, Sun } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'

function getInitialDark(): boolean {
  if (typeof window === 'undefined') return true
  const stored = localStorage.getItem('pulselink-theme')
  if (stored === 'light') return false
  if (stored === 'dark') return true
  return true
}

export function ThemeToggle() {
  const [dark, setDark] = useState(getInitialDark)

  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark)
    localStorage.setItem('pulselink-theme', dark ? 'dark' : 'light')
  }, [dark])

  return (
    <Button
      variant="ghost"
      size="icon"
      aria-label={dark ? 'Switch to light mode' : 'Switch to dark mode'}
      onClick={() => setDark((v) => !v)}
    >
      {dark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </Button>
  )
}
