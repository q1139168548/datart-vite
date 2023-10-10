/**
 * Datart Vite
 *
 * Copyright 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { memo } from 'react';
import { useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { SPACE_LG } from 'styles/StyleConstants';
import styled from 'styled-components';
import { selectOrgId } from 'app/pages/MainPage/slice/selectors';
import { ResourceTypes, SubjectTypes, Viewpoints } from '../constants';
import { ResourcesPermissionSetting } from './ResourcesPermissionSetting';
import { SubjectPermissionSetting } from './SubjectsPermissionSetting';

export const Main = memo(() => {
  const {
    viewpoint,
    type: viewpointType,
    id: viewpointId,
  } = useParams<{
    viewpoint: Viewpoints;
    type: ResourceTypes | SubjectTypes;
    id: string;
  }>();
  const orgId = useSelector(selectOrgId);

  return (
    <Wrapper>
      {viewpoint === Viewpoints.Subject ? (
        <ResourcesPermissionSetting
          viewpoint={viewpoint}
          viewpointId={viewpointId}
          viewpointType={viewpointType}
          orgId={orgId}
        />
      ) : (
        <SubjectPermissionSetting
          viewpoint={viewpoint}
          viewpointId={viewpointId}
          viewpointType={viewpointType}
          orgId={orgId}
        />
      )}
    </Wrapper>
  );
});

const Wrapper = styled.div`
  flex: 1;
  padding: ${SPACE_LG};
  overflow-y: auto;

  .ant-card-head {
    border-bottom: 0;
  }
`;
