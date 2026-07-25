import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { DailyCount } from '@/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

type Props = {
  data: DailyCount[]
  loading: boolean
}

function formatTick(date: string) {
  const d = new Date(`${date}T00:00:00Z`)
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', timeZone: 'UTC' })
}

export function ClicksChart({ data, loading }: Props) {
  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle>Clicks · last 14 days</CardTitle>
        <CardDescription>UTC day buckets from the analytics API</CardDescription>
      </CardHeader>
      <CardContent className="h-64 pt-2 sm:h-72">
        {loading ? (
          <Skeleton className="h-full w-full" />
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
              <defs>
                <linearGradient id="clicksFill" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="var(--color-primary)" stopOpacity={0.45} />
                  <stop offset="100%" stopColor="var(--color-primary)" stopOpacity={0.02} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="color-mix(in oklab, var(--color-foreground) 8%, transparent)" vertical={false} />
              <XAxis
                dataKey="date"
                tickFormatter={formatTick}
                tick={{ fill: 'var(--color-muted-foreground)', fontSize: 11 }}
                axisLine={false}
                tickLine={false}
                minTickGap={28}
              />
              <YAxis
                allowDecimals={false}
                tick={{ fill: 'var(--color-muted-foreground)', fontSize: 11 }}
                axisLine={false}
                tickLine={false}
                width={36}
              />
              <Tooltip
                contentStyle={{
                  background: 'var(--color-background)',
                  border: '1px solid var(--color-card-border)',
                  borderRadius: 12,
                  fontSize: 12,
                }}
                labelFormatter={(v) => formatTick(String(v))}
                formatter={(value) => [value ?? 0, 'Clicks']}
              />
              <Area
                type="monotone"
                dataKey="count"
                stroke="var(--color-primary)"
                strokeWidth={2.5}
                fill="url(#clicksFill)"
                animationDuration={700}
              />
            </AreaChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  )
}
