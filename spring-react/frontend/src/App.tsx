import {Navigate, Route, Routes} from 'react-router-dom'

import Layout from './Layout'
import TaskEditPage from './pages/TaskEditPage'
import TaskMainPage from './pages/TaskMainPage'

export default function App() {
    return (
        <Routes>
            <Route element={<Layout/>}>
                <Route element={<TaskMainPage/>} path="/"/>
                <Route element={<TaskEditPage/>} path="/tasks/:id/edit"/>
            </Route>
            <Route element={<Navigate replace to="/"/>} path="*"/>
        </Routes>
    )
}
