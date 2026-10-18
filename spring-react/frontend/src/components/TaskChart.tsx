import {useRef} from 'react'

type TaskChartEntry = {
    label: string
    value: number
}

type TaskChartProps = Readonly<{
    chartData: TaskChartEntry[]
    maxChartValue: number
}>

export default function TaskChart({chartData, maxChartValue}: Readonly<TaskChartProps>) {
    const svgRef = useRef<SVGSVGElement | null>(null)

    function downloadSvg() {
        if (!svgRef.current) return

        const svgMarkup = new XMLSerializer().serializeToString(svgRef.current)
        const blob = new Blob([svgMarkup], {type: 'image/svg+xml;charset=utf-8'})
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')

        link.href = url
        link.download = 'tasks-per-day.svg'
        document.body.appendChild(link)
        link.click()
        link.remove()
        URL.revokeObjectURL(url)
    }

    return (
        <div style={{marginTop: '2rem'}}>
            <div className="toolbar" style={{marginTop: 0, marginBottom: '1rem'}}>
                <h3 style={{margin: 0}}>Tasks pro Tag</h3>
                <button type="button" onClick={downloadSvg} className="secondary">
                    SVG herunterladen
                </button>
            </div>

            <svg
                ref={svgRef}
                viewBox="0 0 420 180"
                width="100%"
                height="180"
                role="img"
                aria-label="Diagramm der Tasks pro Tag"
            >
                <g>
                    {chartData.map((item, index) => {
                        const barWidth = 32
                        const gap = 18
                        const x = 30 + index * (barWidth + gap)
                        const barHeight = (item.value / maxChartValue) * 110

                        return (
                            <g key={item.label}>
                                <rect
                                    x={x}
                                    y={150 - barHeight}
                                    width={barWidth}
                                    height={barHeight}
                                    rx={5}
                                    fill="#4f46e5"
                                />
                                <text
                                    x={x + barWidth / 2}
                                    y={165}
                                    textAnchor="middle"
                                    fontSize="10"
                                    fill="#333"
                                >
                                    {item.label}
                                </text>
                                <text
                                    x={x + barWidth / 2}
                                    y={145 - barHeight}
                                    textAnchor="middle"
                                    fontSize="10"
                                    fill="#4f46e5"
                                >
                                    {item.value}
                                </text>
                            </g>
                        )
                    })}
                </g>
            </svg>
        </div>
    )
}
