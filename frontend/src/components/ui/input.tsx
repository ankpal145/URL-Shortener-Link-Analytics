import * as React from 'react'
import { cn } from '@/lib/utils'

export const Input = React.forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement>
>(({ className, type, ...props }, ref) => (
  <input
    type={type}
    className={cn(
      'flex h-12 w-full rounded-xl border border-[var(--color-card-border)] bg-[color-mix(in_oklab,var(--color-background)_70%,transparent)] px-4 py-2 text-sm text-[var(--color-foreground)] shadow-inner placeholder:text-[var(--color-muted-foreground)] transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] disabled:cursor-not-allowed disabled:opacity-50',
      className,
    )}
    ref={ref}
    {...props}
  />
))
Input.displayName = 'Input'
