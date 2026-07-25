export type ShortenRequest = {
  url: string
  alias?: string
}

export type ShortenResponse = {
  code: string
  shortUrl: string
  originalUrl: string
  customAlias: boolean
  createdAt: string
}

export type LabeledCount = {
  label: string
  count: number
}

export type DailyCount = {
  date: string
  count: number
}

export type StatsResponse = {
  code: string
  originalUrl: string
  customAlias: boolean
  createdAt: string
  totalClicks: number
  uniqueVisitors: number
  firstClickAt: string | null
  lastClickAt: string | null
  topReferrers: LabeledCount[]
  topUserAgents: LabeledCount[]
  clicksByDay: DailyCount[]
}

export type ClickEventView = {
  clickedAt: string
  referer: string | null
  userAgent: string | null
  ipHash: string | null
}

export type ApiErrorBody = {
  status: number
  error: string
  message: string
  details?: string[]
  timestamp?: string
}

export class ApiError extends Error {
  status: number
  code: string
  details: string[]

  constructor(body: ApiErrorBody) {
    super(body.message || 'Request failed')
    this.name = 'ApiError'
    this.status = body.status
    this.code = body.error
    this.details = body.details ?? []
  }
}

async function parseError(res: Response): Promise<never> {
  let body: ApiErrorBody
  try {
    body = (await res.json()) as ApiErrorBody
  } catch {
    body = {
      status: res.status,
      error: 'unknown',
      message: res.statusText || 'Request failed',
    }
  }
  throw new ApiError(body)
}

export async function shorten(req: ShortenRequest): Promise<ShortenResponse> {
  const res = await fetch('/shorten', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) await parseError(res)
  return res.json() as Promise<ShortenResponse>
}

export async function fetchStats(code: string): Promise<StatsResponse> {
  const res = await fetch(`/stats/${encodeURIComponent(code)}`)
  if (!res.ok) await parseError(res)
  return res.json() as Promise<StatsResponse>
}

export async function fetchClicks(
  code: string,
  limit = 100,
  offset = 0,
): Promise<ClickEventView[]> {
  const params = new URLSearchParams({
    limit: String(limit),
    offset: String(offset),
  })
  const res = await fetch(`/stats/${encodeURIComponent(code)}/clicks?${params}`)
  if (!res.ok) await parseError(res)
  return res.json() as Promise<ClickEventView[]>
}
