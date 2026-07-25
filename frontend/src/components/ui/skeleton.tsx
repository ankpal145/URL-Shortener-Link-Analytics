import { cn } from '@/lib/utils'

export function Skeleton({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        'animate-pulse rounded-xl bg-[color-mix(in_oklab,var(--color-muted)_80%,transparent)]',
        className,
      )}
      {...props}
    />
  )
}
